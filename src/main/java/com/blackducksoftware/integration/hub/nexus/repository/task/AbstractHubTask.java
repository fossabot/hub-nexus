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
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.repository.task.filter.RepositoryWalkerFilter;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public abstract class AbstractHubTask extends AbstractNexusRepositoriesPathAwareTask<Object> {
    protected static final String ALL_REPO_ID = "all_repo";
    private final Walker walker;
    protected ItemAttributesHelper itemAttributesHelper;

    public AbstractHubTask(final Walker walker, final DefaultAttributesHandler attributesHandler) {
        this.walker = walker;
        itemAttributesHelper = new ItemAttributesHelper(attributesHandler);
    }

    public abstract AbstractWalkerProcessor getRepositoryWalker();

    public abstract RepositoryWalkerFilter getRepositoryWalkerFilter();

    @Override
    protected Object doRun() throws Exception {
        try {
            final HubServiceHelper hubServiceHelper = getHubServiceHelper();

            initTask();
            final List<Repository> repositoryList = createRepositoryList();
            final AbstractWalkerProcessor repositoryWalker = getRepositoryWalker();
            final RepositoryWalkerFilter repositoryWalkerFilter = getRepositoryWalkerFilter();

            walkRepositoriesWithFilter(hubServiceHelper, repositoryList, repositoryWalker, repositoryWalkerFilter);
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        }
        return null;
    }

    protected HubServiceHelper getHubServiceHelper() {
        return new HubServiceHelper(new Slf4jIntLogger(logger), this.getParameters());
    }

    public void initTask() throws Exception {
        // Override if needed
    }

    private List<Repository> createRepositoryList() throws NoSuchRepositoryException {
        final String repositoryFieldId = getParameter(TaskField.REPOSITORY_FIELD_ID.getParameterKey());
        if (StringUtils.isNotBlank(repositoryFieldId) && !ALL_REPO_ID.equals(repositoryFieldId)) {
            return Arrays.asList(getRepositoryRegistry().getRepository(repositoryFieldId));
        }
        return getRepositoryRegistry().getRepositories();
    }

    public void walkRepositoriesWithFilter(final HubServiceHelper hubServiceHelper, final List<Repository> repositoryList, final AbstractWalkerProcessor repositoryWalker, final RepositoryWalkerFilter repositoryWalkerFilter) {
        final List<WalkerContext> contextList = new ArrayList<>();

        for (final Repository repository : repositoryList) {
            contextList.add(createRepositoryWalker(repository, hubServiceHelper, repositoryWalker, repositoryWalkerFilter));
        }
        walkRepositories(contextList);
    }

    public WalkerContext createRepositoryWalker(final Repository repository, final HubServiceHelper hubServiceHelper, final AbstractWalkerProcessor repositoryWalker, final RepositoryWalkerFilter scanRepositoryWalkerFilter) {
        final ResourceStoreRequest request = new ResourceStoreRequest(getResourceStorePath(), true, false);
        if (StringUtils.isBlank(request.getRequestPath())) {
            request.setRequestPath(RepositoryItemUid.PATH_ROOT);
        }

        request.setRequestLocalOnly(true);
        final WalkerContext context = new DefaultWalkerContext(repository, request, scanRepositoryWalkerFilter);
        getLogger().info("Creating walker for repository {}", repository.getName());
        context.getProcessors().add(repositoryWalker);
        return context;
    }

    private void walkRepositories(final List<WalkerContext> contextList) {
        for (final WalkerContext context : contextList) {
            try {
                walker.walk(context);
            } catch (final WalkerException walkerEx) {
                if (!(walkerEx.getWalkerContext().getStopCause() instanceof ItemNotFoundException)) {
                    logger.error("Exception walking repository. ", walkerEx);
                }
            }
        }
    }

}
