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
package com.blackducksoftware.integration.hub.nexus.event.handler;

import java.io.File;

import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTaskDescriptor;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class HubScanEventHandler extends HubEventHandler<HubScanEvent> {
    public HubScanEventHandler(final ItemAttributesHelper itemAttributesHelper, final HubScanEvent event, final HubServiceHelper hubServiceHelper) {
        super(itemAttributesHelper, event, hubServiceHelper);
    }

    @Override
    public void run() {
        final HubEventLogger logger = new HubEventLogger(getEvent(), LoggerFactory.getLogger(getClass()));
        try {
            logger.info("Begin handling scan event");
            final HubServiceHelper hubServiceHelper = getHubServiceHelper();
            final String cliInstallRootDirectory = String.format("hub%s", String.valueOf(hubServiceHelper.getHubServerConfig().getHubUrl().getHost().hashCode()));
            logger.info(String.format("CLI Installation Root Directory for %s: %s", hubServiceHelper.getHubServerConfig().getHubUrl().toString(), cliInstallRootDirectory));
            final File blackDuckDirectory = new File(getEvent().getTaskParameters().get(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
            final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);
            final ArtifactScanner scanner = new ArtifactScanner(getEvent(), logger, getAttributeHelper(), taskDirectory, hubServiceHelper);
            final ProjectVersionView projectVersionView = scanner.scan();
            if (projectVersionView != null) {
                logger.info("Posting policy check event for " + projectVersionView.versionName);
                final HubPolicyCheckEvent event = new HubPolicyCheckEvent(getEvent().getRepository(), getEvent().getItem(), getEvent().getTaskParameters(), getEvent().getRequest(), projectVersionView);
                final HubPolicyCheckEventHandler hubPolicyCheckEventHandler = new HubPolicyCheckEventHandler(getAttributeHelper(), event, hubServiceHelper);
                hubPolicyCheckEventHandler.run();
            } else {
                logger.error("Scanned event was null");
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scanning", ex);
        } finally {
            logger.info("Finished handling scan event");
        }
    }
}
