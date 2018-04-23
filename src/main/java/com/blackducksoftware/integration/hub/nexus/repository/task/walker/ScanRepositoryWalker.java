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

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData;
import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.NameVersionNode;
import com.blackducksoftware.integration.hub.nexus.util.ScanAttributesHelper;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;

public class ScanRepositoryWalker extends RepositoryWalkerProcessor<HubScanEvent> {
    private final HubServiceHelper hubServiceHelper;

    public ScanRepositoryWalker(final ScanAttributesHelper scanAttributesHelper, final TaskEventManager taskEventManager, final HubServiceHelper hubServiceHelper, final int maxParallelScans) {
        super(scanAttributesHelper, taskEventManager, maxParallelScans);
        this.hubServiceHelper = hubServiceHelper;
    }

    @Override
    public HubScanEvent createEvent(final WalkerContext context, final StorageItem item) throws IntegrationException {
        final String distribution = scanAttributesHelper.getStringAttribute(TaskField.DISTRIBUTION);
        final String phase = scanAttributesHelper.getStringAttribute(TaskField.PHASE);
        final ProjectRequest projectRequest = createProjectRequest(distribution, phase, item);
        createProjectAndVersion(projectRequest);
        // the walker has already restricted the items to find. Now for scanning to work create a request that is for the repository root because the item path is relative to the repository root
        final ResourceStoreRequest eventRequest = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT, true, false);
        final ScanItemMetaData scanItem = new ScanItemMetaData(item, eventRequest, scanAttributesHelper.getScanAttributes(), projectRequest);
        return processItem(scanItem);
    }

    private HubScanEvent processItem(final ScanItemMetaData data) {
        final HubScanEvent event = new HubScanEvent(data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest(), data.getProjectRequest());
        return event;
    }

    private ProjectRequest createProjectRequest(final String distribution, final String phase, final StorageItem item) {
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
