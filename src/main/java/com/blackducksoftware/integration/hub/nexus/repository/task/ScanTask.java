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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.Walker;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.repository.task.filter.RepositoryWalkerFilter;
import com.blackducksoftware.integration.hub.nexus.repository.task.filter.ScanRepositoryWalkerFilter;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.ScanRepositoryWalker;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

@Named(ScanTaskDescriptor.ID)
public class ScanTask extends AbstractHubTask {
    private final ApplicationConfiguration appConfiguration;

    @Inject
    public ScanTask(final ApplicationConfiguration appConfiguration, final Walker walker, final DefaultAttributesHandler attributesHandler) {
        super(walker, attributesHandler);
        this.appConfiguration = appConfiguration;
    }

    @Override
    protected String getRepositoryFieldId() {
        return TaskField.REPOSITORY_FIELD_ID.getParameterKey();
    }

    @Override
    protected String getRepositoryPathFieldId() {
        return TaskField.REPOSITORY_PATH_FIELD_ID.getParameterKey();
    }

    @Override
    protected String getAction() {
        return "BLACKDUCK_HUB_SCAN";
    }

    @Override
    protected String getMessage() {
        return "HUB-NEXUS-PLUGIN-SCAN: Searching for artifacts to scan in the repository";
    }

    @Override
    public void initTask() throws Exception {
        logger.info("Start task execution.");
        addParameter(TaskField.CURRENT_SCANS.getParameterKey(), "1");
        final HubServiceHelper hubServiceHelper = getHubServiceHelper();
        final File blackDuckDirectory = new File(getParameter(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
        final String cliInstallRootDirectory = String.format("hub%s", String.valueOf(hubServiceHelper.getHubServerConfig().getHubUrl().getHost().hashCode()));
        final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);
        final File cliInstallDirectory = new File(taskDirectory, "tools");
        if (!cliInstallDirectory.exists()) {
            cliInstallDirectory.mkdirs();
        }
        installCLI(cliInstallDirectory, hubServiceHelper);
    }

    @Override
    public AbstractWalkerProcessor getRepositoryWalker() {
        return new ScanRepositoryWalker(appConfiguration, getParameters(), getHubServiceHelper(), getEventBus(), itemAttributesHelper);
    }

    @Override
    public RepositoryWalkerFilter getRepositoryWalkerFilter() {
        final String fileMatchPatterns = getParameter(TaskField.FILE_PATTERNS.getParameterKey());
        return new ScanRepositoryWalkerFilter(fileMatchPatterns, itemAttributesHelper, getParameters());
    }

    private void installCLI(final File installDirectory, final HubServiceHelper hubServiceHelper) throws IntegrationException {
        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Installing CLI to the following location: " + localHostName + ": " + installDirectory);
        final CIEnvironmentVariables ciEnvironmentVariables = new CIEnvironmentVariables();
        ciEnvironmentVariables.putAll(System.getenv());
        final HubVersionRequestService hubVersionRequestService = hubServiceHelper.getHubVersionRequestService();
        final CLIDownloadService cliDownloadService = hubServiceHelper.getCliDownloadService();
        final String hubVersion = hubVersionRequestService.getHubVersion();
        cliDownloadService.performInstallation(installDirectory, ciEnvironmentVariables, hubServiceHelper.getHubServerConfig().getHubUrl().toString(), hubVersion, localHostName);
    }
}
