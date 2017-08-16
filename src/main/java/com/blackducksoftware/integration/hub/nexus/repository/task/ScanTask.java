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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

import com.blackducksoftware.integration.hub.nexus.repository.walker.RepositoryWalker;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

@Named(ScanTaskDescriptor.ID)
public class ScanTask extends AbstractNexusRepositoriesPathAwareTask<Object> {
    private static final String ALL_REPO_ID = "all_repo";
    private final Walker walker;
    private final DefaultAttributesHandler attributesHandler;

    @Inject
    public ScanTask(final Walker walker, final DefaultAttributesHandler attributesHandler) {
        this.walker = walker;
        this.attributesHandler = attributesHandler;
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
        try {

            final String repositoryFieldId = getParameter(TaskField.REPOSITORY_FIELD_ID.getParameterKey());
            List<Repository> repositoryList = new Vector<>();
            final List<WalkerContext> contextList = new ArrayList<>();

            if (StringUtils.isNotBlank(repositoryFieldId)) {
                if (repositoryFieldId.equals(ALL_REPO_ID)) {
                    repositoryList = getRepositoryRegistry().getRepositories();
                } else {
                    repositoryList.add(getRepositoryRegistry().getRepository(repositoryFieldId));
                }
            }
            for (final Repository repository : repositoryList) {
                contextList.add(createRepositoryWalker(repository));
            }
            walkRepositories(contextList);
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
        return "Searching to scan artifacts in the repository";
    }

    private WalkerContext createRepositoryWalker(final Repository repository) {
        final ResourceStoreRequest request = new ResourceStoreRequest(getResourceStorePath(), true, false);
        if (StringUtils.isBlank(request.getRequestPath())) {
            request.setRequestPath(RepositoryItemUid.PATH_ROOT);
        }

        request.setRequestLocalOnly(true);
        final String fileMatchPatterns = getParameter(TaskField.FILE_PATTERNS.getParameterKey());
        final WalkerContext context = new DefaultWalkerContext(repository, request);
        getLogger().info("Creating walker for repository {}", repository.getName());
        context.getProcessors().add(new RepositoryWalker(fileMatchPatterns, new ItemAttributesHelper(attributesHandler), getParameters(), getEventBus()));
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
