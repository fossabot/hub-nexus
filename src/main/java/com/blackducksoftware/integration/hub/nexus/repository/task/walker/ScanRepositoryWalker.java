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

import java.util.Map;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.ScanEventManager;
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.NameVersionNode;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;

public class ScanRepositoryWalker extends AbstractWalkerProcessor {
    private final Logger logger = Loggers.getLogger(getClass());
    private final Map<String, String> taskParameters;
    private final ScanEventManager eventManager;
    private final HubServiceHelper hubServiceHelper;

    public ScanRepositoryWalker(final Map<String, String> taskParameters, final ScanEventManager eventManager, final HubServiceHelper hubServicesHelper) {
        this.taskParameters = taskParameters;
        this.eventManager = eventManager;
        this.hubServiceHelper = hubServicesHelper;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        try {
            logger.info("Item pending scan {}", item);
            final String distribution = taskParameters.get(TaskField.DISTRIBUTION.getParameterKey());
            final String phase = taskParameters.get(TaskField.PHASE.getParameterKey());
            final ProjectRequest projectRequest = createProjectRequest(distribution, phase, item);
            createProjectAndVersion(projectRequest);
            // the walker has already restricted the items to find. Now for scanning to work create a request that is for the repository root because the item path is relative to the repository root
            final ResourceStoreRequest eventRequest = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT, true, false);
            final ScanItemMetaData scanItem = new ScanItemMetaData(item, eventRequest, taskParameters, projectRequest);
            eventManager.processItem(scanItem);
        } catch (final Exception ex) {
            logger.error("Error occurred in walker processor for repository: ", ex);
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
