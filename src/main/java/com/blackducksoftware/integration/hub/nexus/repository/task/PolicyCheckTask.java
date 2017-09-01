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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Named(PolicyCheckTaskDescriptor.ID)
public class PolicyCheckTask extends AbstractHubTask {
    private final AttributesHandler attributesHandler;

    @Inject
    public PolicyCheckTask(final Walker walker, final DefaultAttributesHandler attributesHandler) {
        super(walker);
        this.attributesHandler = attributesHandler;
    }

    @Override
    protected String getRepositoryPathFieldId() {
        return TaskField.REPOSITORY_PATH_FIELD_ID.getParameterKey();
    }

    @Override
    protected Object doRun() throws Exception {
        try {
            final HubServiceHelper hubServiceHelper = new HubServiceHelper(new Slf4jIntLogger(logger), this.getParameters());
            final List<Repository> repositoryList = getRepositoryRegistry().getRepositories();
            final List<WalkerContext> contextList = new ArrayList<>();

            for (final Repository repository : repositoryList) {
                contextList.add(createRepositoryWalker(repository, hubServiceHelper));
            }
            walkRepositories(contextList);
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        }
        return null;
    }

    private WalkerContext createRepositoryWalker(final Repository repository, final HubServiceHelper hubServiceHelper) {
        final ResourceStoreRequest request = new ResourceStoreRequest(getResourceStorePath(), true, false);
        if (StringUtils.isBlank(request.getRequestPath())) {
            request.setRequestPath(RepositoryItemUid.PATH_ROOT);
        }

        request.setRequestLocalOnly(true);
        final WalkerContext context = new DefaultWalkerContext(repository, request);
        getLogger().info("Creating walker for repository {}", repository.getName());
        context.getProcessors().add(new PolicyRepositoryWalker(getEventBus(), new ItemAttributesHelper(attributesHandler), getParameters(), hubServiceHelper));
        return context;
    }

    @Override
    protected String getAction() {
        return "BLACKDUCK_HUB_POLICY_CHECK";
    }

    @Override
    protected String getMessage() {
        return "HUB-NEXUS-PLUGIN-POLICY-CHECK: Search for successfully scanned artifacts and check their policy";
    }

}
