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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public abstract class AbstractWalkerHubTask extends AbstractNexusRepositoriesPathAwareTask<Object> {
    protected static final String ALL_REPO_ID = "all_repo";
    protected ItemAttributesHelper itemAttributesHelper;
    protected TaskWalker taskWalker;
    private HubServiceHelper hubServiceHelper;

    public AbstractWalkerHubTask(final TaskWalker taskWalker, final DefaultAttributesHandler attributesHandler) {
        this.taskWalker = taskWalker;
        itemAttributesHelper = new ItemAttributesHelper(attributesHandler);
    }

    protected HubServiceHelper getHubServiceHelper() {
        if (hubServiceHelper == null) {
            hubServiceHelper = new HubServiceHelper(new Slf4jIntLogger(logger), getParameters());
        }

        return hubServiceHelper;
    }

    private List<Repository> createRepositoryList() {
        final String repositoryFieldId = getParameter(TaskField.REPOSITORY_FIELD_ID.getParameterKey());
        if (StringUtils.isNotBlank(repositoryFieldId) && !ALL_REPO_ID.equals(repositoryFieldId)) {
            try {
                return Arrays.asList(getRepositoryRegistry().getRepository(repositoryFieldId));
            } catch (final NoSuchRepositoryException e) {
                logger.warn("No repositories found to walk");
                return Arrays.asList();
            }
        }
        return getRepositoryRegistry().getRepositories();
    }

    @Override
    protected final Object doRun() throws Exception {
        try {
            initTask();
            final AbstractWalkerProcessor repositoryWalker = getRepositoryWalker();
            final DefaultStoreWalkerFilter repositoryWalkerFilter = getRepositoryWalkerFilter();

            final List<Repository> repositoryList = createRepositoryList();
            taskWalker.walkRepositoriesWithFilter(repositoryList, repositoryWalker, repositoryWalkerFilter, getResourceStorePath());
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        }
        return null;
    }

    protected void initTask() throws Exception {
        // Override if needed
    }

    protected abstract AbstractWalkerProcessor getRepositoryWalker();

    protected abstract DefaultStoreWalkerFilter getRepositoryWalkerFilter();

}
