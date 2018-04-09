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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.Walker;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.repository.task.filter.PolicyRepositoryWalkerFilter;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.PolicyRepositoryWalker;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Named(PolicyCheckTaskDescriptor.ID)
public class PolicyCheckTask extends AbstractHubTask {

    @Inject
    public PolicyCheckTask(final Walker walker, final DefaultAttributesHandler attributesHandler) {
        super(walker, attributesHandler);
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

            final PolicyRepositoryWalker policyRepositoryWalker = new PolicyRepositoryWalker(getEventBus(), itemAttributesHelper, getParameters(), hubServiceHelper);
            final PolicyRepositoryWalkerFilter policyRepositoryWalkerFilter = new PolicyRepositoryWalkerFilter(itemAttributesHelper);
            walkRepositoriesWithFilter(hubServiceHelper, repositoryList, policyRepositoryWalker, policyRepositoryWalkerFilter);
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        }
        return null;
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
