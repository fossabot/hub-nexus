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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;

import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.PolicyRepositoryWalker;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.TaskWalker;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.filter.PolicyRepositoryWalkerFilter;
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor;
import com.blackducksoftware.integration.hub.nexus.util.ScanAttributesHelper;

@Named(PolicyCheckTaskDescriptor.ID)
public class PolicyCheckTask extends AbstractHubWalkerTask {
    private final ParallelEventProcessor parallelEventProcessor;

    @Inject
    public PolicyCheckTask(final TaskWalker walker, final DefaultAttributesHandler attributesHandler, final ParallelEventProcessor parallelEventProcessor, final IntegrationInfo integrationInfo) {
        super(walker, attributesHandler, integrationInfo);
        this.parallelEventProcessor = parallelEventProcessor;
    }

    @Override
    protected String getRepositoryPathFieldId() {
        return TaskField.REPOSITORY_PATH_FIELD_ID.getParameterKey();
    }

    @Override
    protected String getAction() {
        return "BLACKDUCK_HUB_POLICY_CHECK";
    }

    @Override
    protected String getMessage() {
        return "HUB-NEXUS-PLUGIN-POLICY-CHECK: Search for successfully scanned artifacts and check their policy";
    }

    @Override
    protected List<Repository> createRepositoryList() {
        final List<Repository> repositoryList = new ArrayList<>();
        final List<Repository> allRepositoryList = super.createRepositoryList();
        for (final Repository repository : allRepositoryList) {
            if (!repository.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
                repositoryList.add(repository);
            }
        }

        return repositoryList;
    }

    @Override
    protected AbstractWalkerProcessor getRepositoryWalker() {
        return new PolicyRepositoryWalker(parallelEventProcessor, itemAttributesHelper, new ScanAttributesHelper(getParameters()), getHubServiceHelper());
    }

    @Override
    protected DefaultStoreWalkerFilter getRepositoryWalkerFilter() {
        return new PolicyRepositoryWalkerFilter(itemAttributesHelper);
    }

    @Override
    protected void afterRun() throws Exception {
        parallelEventProcessor.shutdownProcessor();
    }

}
