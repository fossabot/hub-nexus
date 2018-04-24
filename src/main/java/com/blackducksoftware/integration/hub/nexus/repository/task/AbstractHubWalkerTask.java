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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.TaskWalker;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public abstract class AbstractHubWalkerTask extends AbstractNexusRepositoriesPathAwareTask<Object> {
    protected static final String ALL_REPO_ID = "all_repo";
    protected ItemAttributesHelper itemAttributesHelper;
    protected TaskWalker taskWalker;
    private HubServiceHelper hubServiceHelper;
    private final IntegrationInfo integrationInfo;

    public AbstractHubWalkerTask(final TaskWalker taskWalker, final DefaultAttributesHandler attributesHandler, final IntegrationInfo integrationInfo) {
        this.taskWalker = taskWalker;
        itemAttributesHelper = new ItemAttributesHelper(attributesHandler);
        this.integrationInfo = integrationInfo;
    }

    protected HubServiceHelper getHubServiceHelper() {
        if (hubServiceHelper == null) {
            hubServiceHelper = new HubServiceHelper(new Slf4jIntLogger(logger), getParameters());
        }

        return hubServiceHelper;
    }

    protected List<Repository> createRepositoryList() {
        final List<Repository> repoExcludingProxies = new ArrayList<>();
        final String repositoryFieldId = getParameter(TaskField.REPOSITORY_FIELD_ID.getParameterKey());
        if (StringUtils.isNotBlank(repositoryFieldId) && !ALL_REPO_ID.equals(repositoryFieldId)) {
            try {
                return Arrays.asList(getRepositoryRegistry().getRepository(repositoryFieldId));
            } catch (final NoSuchRepositoryException e) {
                logger.warn("No repositories found to walk");
                return Arrays.asList();
            }
        }

        for (final Repository repository : getRepositoryRegistry().getRepositories()) {
            if (!repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
                repoExcludingProxies.add(repository);
            }
        }
        return repoExcludingProxies;
    }

    @Override
    protected final Object doRun() throws Exception {
        try {
            initTask();
            final AbstractWalkerProcessor repositoryWalker = getRepositoryWalker();
            final DefaultStoreWalkerFilter repositoryWalkerFilter = getRepositoryWalkerFilter();

            final List<Repository> repositoryList = createRepositoryList();
            taskWalker.walkRepositoriesWithFilter(repositoryList, repositoryWalker, repositoryWalkerFilter, getResourceStorePath());
            phoneHome();
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        }
        return null;
    }

    private void phoneHome() {
        // TODO add phonehome here that references the last time phone home was called
    }

    protected void initTask() throws Exception {
        // Override if needed
    }

    protected abstract AbstractWalkerProcessor getRepositoryWalker();

    protected abstract DefaultStoreWalkerFilter getRepositoryWalkerFilter();

}
