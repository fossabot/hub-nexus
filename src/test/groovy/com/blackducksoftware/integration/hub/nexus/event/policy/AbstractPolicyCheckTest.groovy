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
package com.blackducksoftware.integration.hub.nexus.event.policy

import com.blackducksoftware.integration.hub.nexus.event.AbstractHandlerTest
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTaskDescriptor
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import org.slf4j.LoggerFactory

public abstract class AbstractPolicyCheckTest extends AbstractHandlerTest {
    HubPolicyCheckEvent policyCheckEvent

    @Override
    protected void setUp() throws Exception {
        super.setUp()

        getTaskParameters().put(TaskField.WORKING_DIRECTORY.getParameterKey(), getWorkHomeDir().getCanonicalPath())
        getTaskParameters().put(TaskField.HUB_SCAN_MEMORY.getParameterKey(), "4096")
        getTaskParameters().put(TaskField.HUB_TIMEOUT.getParameterKey(), "300")
        final ScanItemMetaData data = new ScanItemMetaData(getItem(), getResourceStoreRequest(), getTaskParameters(), getProjectRequest())
        HubScanEvent event = processItem(data);

        final HubEventLogger logger = new HubEventLogger(event, LoggerFactory.getLogger(getClass()));

        String cliInstallRootDirectory = String.format("hub%s", String.valueOf(hubServiceHelper.getHubServerConfig().getHubUrl().getHost().hashCode()));
        final File blackDuckDirectory = new File(event.getTaskParameters().get(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
        final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);

        final ArtifactScanner scanner = new ArtifactScanner(event, logger, getItemAttributesHelper(), taskDirectory, hubServiceHelper);
        ProjectVersionWrapper projectVersionWrapper = scanner.scan();

        policyCheckEvent = new HubPolicyCheckEvent(event.getRepository(), event.getItem(), event.getTaskParameters(), event.getRequest(), projectVersionWrapper.getProjectVersionView());
    }
}
