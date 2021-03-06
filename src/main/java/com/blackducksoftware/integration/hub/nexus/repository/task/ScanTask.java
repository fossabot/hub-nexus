/*
 * hub-nexus
 *
 * 	Copyright (C) 2019 Black Duck Software, Inc.
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

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;

import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.ScanRepositoryWalker;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.TaskWalker;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.filter.ScanRepositoryWalkerFilter;
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor;

@Named(ScanTaskDescriptor.ID)
public class ScanTask extends AbstractHubWalkerTask {

    @Inject
    public ScanTask(final IntegrationInfo integrationInfo, final TaskWalker walker, final DefaultAttributesHandler attributesHandler, final ParallelEventProcessor parallelEventProcessor) {
        super(walker, attributesHandler, integrationInfo, parallelEventProcessor);
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
    }

    @Override
    public AbstractWalkerProcessor getRepositoryWalker() {
        parallelEventProcessor.initializeExecutorService(Runtime.getRuntime().availableProcessors() + 1 / 2);
        return new ScanRepositoryWalker(parallelEventProcessor, getParameters(), getHubServiceHelper(), itemAttributesHelper);
    }

    @Override
    public DefaultStoreWalkerFilter getRepositoryWalkerFilter() {
        final String fileMatchPatterns = getParameter(TaskField.FILE_PATTERNS.getParameterKey());
        return new ScanRepositoryWalkerFilter(fileMatchPatterns, itemAttributesHelper, getParameters());
    }

}
