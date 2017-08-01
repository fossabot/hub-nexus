/*
 * hub-nexus
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.nexus.repository.walker.RepositoryWalker;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Named(ScanTaskDescriptor.ID)
public class ScanTask extends AbstractNexusRepositoriesPathAwareTask<Object> {

    private static final String ALL_REPO_ID = "all_repo";

    private final ApplicationConfiguration appConfiguration;

    private final Walker walker;

    private final String HUB_URL = "";

    private final int HUB_TIMEOUT = 120;

    private final String HUB_USERNAME = "";

    private final String HUB_PASSWORD = "";

    private final String HUB_PROXY_HOST = "";

    private final int HUB_PROXY_PORT = 0;

    private final String HUB_PROXY_USERNAME = "";

    private final String HUB_PROXY_PASSWORD = "";

    @Inject
    public ScanTask(final ApplicationConfiguration appConfiguration, final Walker walker) {
        this.appConfiguration = appConfiguration;
        this.walker = walker;
    }

    @Override
    protected String getRepositoryFieldId() {
        return ScanTaskDescriptor.REPOSITORY_FIELD_ID;
    }

    @Override
    protected String getRepositoryPathFieldId() {
        return ScanTaskDescriptor.REPOSITORY_PATH_FIELD_ID;
    }

    @Override
    protected Object doRun() throws Exception {
        getLogger().info("Running Scan repository task");
        final String repositoryFieldId = getParameter(ScanTaskDescriptor.REPOSITORY_FIELD_ID);
        final String pathList = getParameter(ScanTaskDescriptor.REPOSITORY_PATH_FIELD_ID);
        getLogger().info(String.format("Repository ID %s", repositoryFieldId));
        getLogger().info(String.format("Repository Path Field ID %s", pathList));
        List<Repository> repositoryList = new Vector<>();
        final List<WalkerContext> contextList = new ArrayList<>();

        if (StringUtils.isNotBlank(repositoryFieldId)) {
            if (repositoryFieldId.equals(ALL_REPO_ID)) {
                repositoryList = getRepositoryRegistry().getRepositories();
            } else {
                repositoryList.add(getRepositoryRegistry().getRepository(repositoryFieldId));
            }
        }
        final HubServerConfig hubServerConfig = createHubServerConfig();
        final CredentialsRestConnection credentialsRestConnection = hubServerConfig.createCredentialsRestConnection(new Slf4jIntLogger(logger));
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(credentialsRestConnection);
        for (final Repository repository : repositoryList) {
            contextList.add(createRepositoryWalker(hubServerConfig, hubServicesFactory, repository));
        }
        walkRepositories(contextList);
        return null;
    }

    @Override
    protected String getAction() {
        return "HUB_SCAN";
    }

    @Override
    protected String getMessage() {
        return "Searching to scan artifacts in the repository";
    }

    private HubServerConfig createHubServerConfig() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(HUB_URL);
        hubServerConfigBuilder.setUsername(HUB_USERNAME);
        hubServerConfigBuilder.setPassword(HUB_PASSWORD);
        hubServerConfigBuilder.setTimeout(HUB_TIMEOUT);
        hubServerConfigBuilder.setProxyHost(HUB_PROXY_HOST);
        hubServerConfigBuilder.setProxyPort(HUB_PROXY_PORT);
        hubServerConfigBuilder.setProxyUsername(HUB_PROXY_USERNAME);
        hubServerConfigBuilder.setProxyPassword(HUB_PROXY_PASSWORD);

        return hubServerConfigBuilder.build();
    }

    private WalkerContext createRepositoryWalker(final HubServerConfig hubServerConfig, final HubServicesFactory hubServicesFactory,
            final Repository repository) {
        final ResourceStoreRequest request = new ResourceStoreRequest(getResourceStorePath(), true, false);
        if (StringUtils.isBlank(request.getRequestPath())) {
            request.setRequestPath(RepositoryItemUid.PATH_ROOT);
        }

        request.setRequestLocalOnly(true);

        final WalkerContext context = new DefaultWalkerContext(repository, request);
        getLogger().info(String.format("Creating walker for repository %s", repository.getName()));
        context.getProcessors().add(new RepositoryWalker(hubServerConfig, hubServicesFactory));
        return context;
    }

    private void walkRepositories(final List<WalkerContext> contextList) {
        try {
            for (final WalkerContext context : contextList) {
                walker.walk(context);
            }
        } catch (final Exception ex) {
            throw ex;
        }
    }
}
