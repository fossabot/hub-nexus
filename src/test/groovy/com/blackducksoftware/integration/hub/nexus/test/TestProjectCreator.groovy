/*
 * hub-nexus
 *
 * 	Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.nexus.test

import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum
import com.blackducksoftware.integration.hub.model.request.ProjectRequest
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView
import com.blackducksoftware.integration.hub.model.view.ProjectView
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.log.IntLogger

public class TestProjectCreator {
    private final HubServicesFactory hubServicesFactory
    private final ProjectRequestService projectRequestService
    private final ProjectVersionRequestService projectVersionRequestService
    private final IntLogger logger

    public TestProjectCreator(final RestConnectionTestHelper restConnectionTestHelper, final IntLogger logger) throws Exception {
        this.logger = logger
        hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        projectRequestService = hubServicesFactory.createProjectRequestService(logger)
        projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService(logger)
    }

    public String createProject(final String name, final ProjectVersionDistributionEnum distribution, final ProjectVersionPhaseEnum phase, final String version) throws IntegrationException {
        final ProjectRequest projectRequest = new ProjectRequest(name)
        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest(distribution, phase, version)

        projectRequest.setVersionRequest(projectVersionRequest)

        final String projectName = projectRequestService.createHubProject(projectRequest)

        return projectName
    }

    public ProjectView getProjectViewCreateIfNeeded(final String name, final ProjectVersionDistributionEnum distribution, final ProjectVersionPhaseEnum phase, final String version) throws IntegrationException {
        ProjectView projectView
        try {
            projectView = getProjectView(name)
        } catch (final Exception e) {
            createProject(name, distribution, phase, version)
            projectView = getProjectView(name)
        }

        return projectView
    }

    public ProjectView getProjectView(final String name) throws IntegrationException {
        return projectRequestService.getProjectByName(name)
    }

    public ProjectVersionView getProjectVersionView(final ProjectView project, final String projectVersionName) throws IntegrationException {
        return projectVersionRequestService.getProjectVersion(project, projectVersionName)
    }

    public void destroyProject(final ProjectView projectView) throws IntegrationException {
        projectRequestService.deleteHubProject(projectView)
    }
}
