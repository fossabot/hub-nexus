/*
 * hub-nexus
 *
 * 	Copyright (C) 2019 Black Duck Software, Inc.
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
import java.util.Optional;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData;
import com.blackducksoftware.integration.hub.nexus.event.handler.HubEventHandler;
import com.blackducksoftware.integration.hub.nexus.event.handler.HubScanEventHandler;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.NameVersionNode;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.TagView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.TagService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

public class ScanRepositoryWalker extends RepositoryWalkerProcessor<HubScanEvent> {
    public static final String NEXUS_PROJECT_TAG = "hub_nexus";
    protected final Logger logger = Loggers.getLogger(getClass());

    private final HubServiceHelper hubServiceHelper;
    private final ItemAttributesHelper itemAttributesHelper;
    private final Map<String, String> taskParams;

    public ScanRepositoryWalker(final ParallelEventProcessor parallelEventProcessor, final Map<String, String> taskParams, final HubServiceHelper hubServiceHelper, final ItemAttributesHelper itemAttributesHelper) {
        super(parallelEventProcessor);
        this.hubServiceHelper = hubServiceHelper;
        this.itemAttributesHelper = itemAttributesHelper;
        this.taskParams = taskParams;
    }

    @Override
    public HubEventHandler<HubScanEvent> getHubEventHandler(final WalkerContext context, final StorageItem item) throws IntegrationException {
        final HubScanEvent event = createEvent(context, item);
        final HubScanEventHandler hubScanEventHandler = new HubScanEventHandler(itemAttributesHelper, event, hubServiceHelper);
        return hubScanEventHandler;
    }

    public HubScanEvent createEvent(final WalkerContext context, final StorageItem item) throws IntegrationException {
        final String distribution = taskParams.get(TaskField.DISTRIBUTION.getParameterKey());
        final String phase = taskParams.get(TaskField.PHASE.getParameterKey());
        final NameVersionNode nameVersionNode = generateProjectNameVersion(item);

        final BlackDuckServicesFactory blackDuckServicesFactory = hubServiceHelper.createBlackDuckServicesFactory();

        final ProjectVersionWrapper projectVersionWrapper = getOrCreateProjectVersion(blackDuckServicesFactory.createBlackDuckService(), blackDuckServicesFactory.createProjectService(), nameVersionNode.getName(),
            nameVersionNode.getVersion(), distribution,
            phase);

        // the walker has already restricted the items to find. Now for scanning to work create a request that is for the repository root because the item path is relative to the repository root
        final ResourceStoreRequest eventRequest = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT, true, false);
        final ScanItemMetaData scanItem = new ScanItemMetaData(item, eventRequest, taskParams, projectVersionWrapper);
        return processItem(scanItem);
    }

    private HubScanEvent processItem(final ScanItemMetaData data) {
        final HubScanEvent event = new HubScanEvent(data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest(), data.getProjectVersionWrapper());
        return event;
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

    public ProjectVersionWrapper getOrCreateProjectVersion(final BlackDuckService blackDuckService, final ProjectService projectService, final String name, final String versionName, final String distribution, final String phase)
        throws IntegrationException {
        final Optional<ProjectVersionWrapper> projectVersionWrapperOptional = projectService.getProjectVersion(name, versionName);
        final ProjectVersionWrapper projectVersionWrapper;
        if (projectVersionWrapperOptional.isPresent()) {
            projectVersionWrapper = projectVersionWrapperOptional.get();
        } else {
            projectVersionWrapper = createProjectVersion(projectService, name, versionName, distribution, phase);
        }

        final TagService tagService = new TagService(blackDuckService, new Slf4jIntLogger(logger));
        final ProjectView projectView = projectVersionWrapper.getProjectView();
        final Optional<TagView> matchingTag = tagService.findMatchingTag(projectView, NEXUS_PROJECT_TAG);
        if (!matchingTag.isPresent()) {
            logger.debug("Adding tag {} to project {} in Black Duck.", NEXUS_PROJECT_TAG, name);
            final TagView tagView = new TagView();
            tagView.setName(NEXUS_PROJECT_TAG);
            tagService.createTag(projectView, tagView);
        }

        return projectVersionWrapper;
    }

    private ProjectVersionWrapper createProjectVersion(final ProjectService projectService, final String name, final String versionName, final String distribution, final String phase) throws IntegrationException {
        logger.debug("Creating project in Black Duck : {}", name);
        final ProjectSyncModel projectSyncModel = new ProjectSyncModel();
        projectSyncModel.setName(name);
        projectSyncModel.setVersionName(versionName);
        projectSyncModel.setProjectLevelAdjustments(true);
        projectSyncModel.setPhase(ProjectVersionPhaseType.valueOf(phase.toUpperCase()));
        projectSyncModel.setDistribution(ProjectVersionDistributionType.valueOf(distribution.toUpperCase()));
        return projectService.createProject(projectSyncModel.createProjectRequest());
    }

}
