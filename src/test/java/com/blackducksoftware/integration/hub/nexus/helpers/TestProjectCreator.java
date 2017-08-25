/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.nexus.helpers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;

public class TestProjectCreator {
    private final HubServicesFactory hubServicesFactory;
    private final ProjectRequestService projectRequestService;
    private final ProjectVersionRequestService projectVersionRequestService;
    private final IntLogger logger;

    public TestProjectCreator(final RestConnectionTestHelper restConnectionTestHelper, final IntLogger logger) throws Exception {
        this.logger = logger;
        hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        projectRequestService = hubServicesFactory.createProjectRequestService(logger);
        projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService(logger);
    }

    public String createProject(final String name, final ProjectVersionDistributionEnum distribution, final ProjectVersionPhaseEnum phase, final String version) throws IntegrationException {
        final ProjectRequest projectRequest = new ProjectRequest(name);
        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest(distribution, phase, version);

        projectRequest.setVersionRequest(projectVersionRequest);

        final String projectName = projectRequestService.createHubProject(projectRequest);

        return projectName;
    }

    public ProjectView getProjectViewCreateIfNeeded(final String name, final ProjectVersionDistributionEnum distribution, final ProjectVersionPhaseEnum phase, final String version) throws IntegrationException {
        ProjectView projectView;
        try {
            projectView = getProjectView(name);
        } catch (final Exception e) {
            createProject(name, distribution, phase, version);
            projectView = getProjectView(name);
        }

        return projectView;
    }

    public ProjectView getProjectView(final String name) throws IntegrationException {
        return projectRequestService.getProjectByName(name);
    }

    public ProjectVersionView getProjectVersionView(final ProjectView project, final String projectVersionName) throws IntegrationException {
        return projectVersionRequestService.getProjectVersion(project, projectVersionName);
    }

    public void destroyProject(final ProjectView projectView) throws IntegrationException {
        projectRequestService.deleteHubProject(projectView);
    }
}
