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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.ScanEventManager;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

@Named(ScanTaskDescriptor.ID)
public class ScanTask extends AbstractHubTask {
    private static final String ALL_REPO_ID = "all_repo";
    private final DefaultAttributesHandler attributesHandler;
    private final ScanEventManager eventManager;

    @Inject
    public ScanTask(final Walker walker, final DefaultAttributesHandler attributesHandler, final ScanEventManager eventManager) {
        super(walker);
        this.attributesHandler = attributesHandler;
        this.eventManager = eventManager;
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
    protected Object doRun() throws Exception {
        final HubServiceHelper hubServiceHelper = new HubServiceHelper(new Slf4jIntLogger(logger), this.getParameters());
        File blackDuckDirectory = null;
        try {
            final int pendingEvents = eventManager.pendingEventCount(getName());
            if (pendingEvents > 0) {
                logger.info("{} pending events from the last run.  Skipping task execution.", pendingEvents);
            } else {
                logger.info("No Pending events for this task.  Start task execution.");
                blackDuckDirectory = new File(getParameter(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
                final String cliInstallRootDirectory = String.format("hub%s", String.valueOf(hubServiceHelper.getHubServerConfig().getHubUrl().getHost().hashCode()));
                final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);
                final File cliInstallDirectory = new File(taskDirectory, "tools");
                if (!cliInstallDirectory.exists()) {
                    cliInstallDirectory.mkdirs();
                }
                installCLI(cliInstallDirectory, hubServiceHelper);
                final String repositoryFieldId = getParameter(TaskField.REPOSITORY_FIELD_ID.getParameterKey());
                final List<Repository> repositoryList = createRepositoryList(repositoryFieldId);
                final List<WalkerContext> contextList = new ArrayList<>();

                for (final Repository repository : repositoryList) {
                    contextList.add(createRepositoryWalker(eventManager, repository, hubServiceHelper));
                }
                walkRepositories(contextList);
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        }
        return null;
    }

    @Override
    protected String getAction() {
        return "BLACKDUCK_HUB_SCAN";
    }

    @Override
    protected String getMessage() {
        return "HUB-NEXUS-PLUGIN-SCAN: Searching for artifacts to scan in the repository";
    }

    private List<Repository> createRepositoryList(final String repositoryFieldId) throws NoSuchRepositoryException {
        List<Repository> repositoryList = new Vector<>();
        if (StringUtils.isNotBlank(repositoryFieldId)) {
            if (repositoryFieldId.equals(ALL_REPO_ID)) {
                repositoryList = getRepositoryRegistry().getRepositories();
            } else {
                repositoryList.add(getRepositoryRegistry().getRepository(repositoryFieldId));
            }
        }
        return repositoryList;
    }

    private WalkerContext createRepositoryWalker(final ScanEventManager eventManager, final Repository repository, final HubServiceHelper hubServiceHelper) {
        final ResourceStoreRequest request = new ResourceStoreRequest(getResourceStorePath(), true, false);
        if (StringUtils.isBlank(request.getRequestPath())) {
            request.setRequestPath(RepositoryItemUid.PATH_ROOT);
        }

        request.setRequestLocalOnly(true);
        final String fileMatchPatterns = getParameter(TaskField.FILE_PATTERNS.getParameterKey());
        final WalkerContext context = new DefaultWalkerContext(repository, request);
        getLogger().info("Creating walker for repository {}", repository.getName());
        context.getProcessors().add(new ScanRepositoryWalker(fileMatchPatterns, new ItemAttributesHelper(attributesHandler), getParameters(), eventManager, hubServiceHelper));
        return context;
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
