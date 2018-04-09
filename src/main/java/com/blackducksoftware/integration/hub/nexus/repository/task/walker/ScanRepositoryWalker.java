/*
 * hub-nexus
 *
 * 	Copyright (C) 2018 Black Duck Software, Inc.
 * 	http://www.blackducksoftware.com/
 *
 * 	Licensed to the Apache Software Foundation (ASF) under one
 * 	or more contributor license agreements. See the NOTICE file
 * 	distributed with this work for additional information
 * 	regarding copyright ownership. The ASF licenses this file
 * 	to you under the Apache License, Version 2.0 (the
 * 	"License"); you may not use this file except in compliance
 * 	with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an
 * 	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * 	KIND, either express or implied. See the License for the
 * 	specific language governing permissions and limitations
 * 	under the License.
 */
package com.blackducksoftware.integration.hub.nexus.repository.task.walker;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData;
import com.blackducksoftware.integration.hub.nexus.exception.MaxScansException;
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTaskDescriptor;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner;
import com.blackducksoftware.integration.hub.nexus.scan.NameVersionNode;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;

public class ScanRepositoryWalker extends AbstractWalkerProcessor {
    private final Logger logger = Loggers.getLogger(getClass());
    private final ApplicationConfiguration appConfiguration;
    private final Map<String, String> taskParameters;
    private final HubServiceHelper hubServiceHelper;
    private final EventBus eventBus;
    private final ItemAttributesHelper itemAttributesHelper;

    public ScanRepositoryWalker(final ApplicationConfiguration appConfiguration, final Map<String, String> taskParameters, final HubServiceHelper hubServicesHelper, final EventBus eventBus,
            final ItemAttributesHelper itemAttributesHelper) {
        this.appConfiguration = appConfiguration;
        this.taskParameters = taskParameters;
        this.hubServiceHelper = hubServicesHelper;
        this.eventBus = eventBus;
        this.itemAttributesHelper = itemAttributesHelper;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        boolean shouldProcess = true;
        final String currentScansString = taskParameters.get(TaskField.CURRENT_SCANS.getParameterKey());
        int currentScans = Integer.parseInt(currentScansString);
        final String maxScansString = taskParameters.get(TaskField.MAX_SCANS.getParameterKey());
        if (!StringUtils.isEmpty(maxScansString)) {
            final int maxScans = Integer.parseInt(maxScansString);
            shouldProcess = currentScans <= maxScans;
        }

        if (shouldProcess) {
            logger.info("Scanning item number " + currentScans);
            processItem(item);
            currentScans++;
            taskParameters.put(TaskField.CURRENT_SCANS.getParameterKey(), String.valueOf(currentScans));
        } else {
            context.stop(new MaxScansException(currentScans - 1));
        }
    }

    public void processItem(final StorageItem item) {
        try {
            logger.info("Item pending scan {}", item);
            final String distribution = taskParameters.get(TaskField.DISTRIBUTION.getParameterKey());
            final String phase = taskParameters.get(TaskField.PHASE.getParameterKey());
            final ProjectRequest projectRequest = createProjectRequest(distribution, phase, item);
            createProjectAndVersion(projectRequest);
            // the walker has already restricted the items to find. Now for scanning to work create a request that is for the repository root because the item path is relative to the repository root
            final ResourceStoreRequest eventRequest = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT, true, false);
            final ScanItemMetaData scanItem = new ScanItemMetaData(item, eventRequest, taskParameters, projectRequest);
            final HubScanEvent scanEvent = processMetaData(scanItem);
            scanItem(scanEvent);
        } catch (final Exception ex) {
            itemAttributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_FAILED);
            logger.error("Error occurred during scanning", ex);
        } finally {
            logger.info("Finished handling scan event");
        }
    }

    public HubScanEvent processMetaData(final ScanItemMetaData data) throws InterruptedException {
        final HubScanEvent event = new HubScanEvent(data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest(), data.getProjectRequest());
        return event;
    }

    public void scanItem(final HubScanEvent event) {
        logger.info("Begin handling scan event");
        final IntegrationInfo phoneHomeInfo = new IntegrationInfo(ThirdPartyName.NEXUS, appConfiguration.getConfigurationModel().getNexusVersion(), ScanTaskDescriptor.PLUGIN_VERSION);
        final String cliInstallRootDirectory = String.format("hub%s", String.valueOf(hubServiceHelper.getHubServerConfig().getHubUrl().getHost().hashCode()));
        logger.info(String.format("CLI Installation Root Directory for %s: %s", hubServiceHelper.getHubServerConfig().getHubUrl().toString(), cliInstallRootDirectory));
        final File blackDuckDirectory = new File(event.getTaskParameters().get(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
        final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);
        final ArtifactScanner scanner = new ArtifactScanner(event, itemAttributesHelper, taskDirectory, hubServiceHelper, phoneHomeInfo);
        final ProjectVersionView projectVersionView = scanner.scan();
        if (projectVersionView != null) {
            logger.info("Posting policy check event for " + projectVersionView.versionName);
            eventBus.post(new HubPolicyCheckEvent(event.getRepository(), event.getItem(), event.getTaskParameters(), event.getRequest(), projectVersionView));
        }
    }

    public ProjectRequest createProjectRequest(final String distribution, final String phase, final StorageItem item) {
        final ProjectRequestBuilder builder = new ProjectRequestBuilder();
        final NameVersionNode nameVersion = generateProjectNameVersion(item);
        builder.setProjectName(nameVersion.getName());
        builder.setVersionName(nameVersion.getVersion());
        builder.setProjectLevelAdjustments(true);
        builder.setPhase(phase.toUpperCase());
        builder.setDistribution(distribution.toUpperCase());
        return builder.build();
    }

    private NameVersionNode generateProjectNameVersion(final StorageItem item) {
        final String path = item.getParentPath();
        String name = item.getName();
        String version = "0.0.0";
        final String[] pathSections = path.split("/");
        if (pathSections.length > 1) {
            version = pathSections[pathSections.length - 1];
            name = pathSections[pathSections.length - 2];
        }
        final NameVersionNode nameVersion = new NameVersionNode(name, version);
        return nameVersion;
    }

    private void createProjectAndVersion(final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null;
        final ProjectRequestService projectRequestService = hubServiceHelper.getProjectRequestService();
        final ProjectVersionRequestService projectVersionRequestService = hubServiceHelper.getProjectVersionRequestService();
        try {
            project = projectRequestService.getProjectByName(projectRequest.getName());
        } catch (final DoesNotExistException e) {
            final String projectURL = projectRequestService.createHubProject(projectRequest);
            project = projectRequestService.getItem(projectURL, ProjectView.class);
        }
        try {
            projectVersionRequestService.getProjectVersion(project, projectRequest.getVersionRequest().getVersionName());
        } catch (final DoesNotExistException e) {
            final String versionURL = projectVersionRequestService.createHubVersion(project, projectRequest.getVersionRequest());
            projectVersionRequestService.getItem(versionURL, ProjectVersionView.class);
        }
    }
}
