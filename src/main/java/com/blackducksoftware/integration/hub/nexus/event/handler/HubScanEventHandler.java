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
package com.blackducksoftware.integration.hub.nexus.event.handler;

import java.net.URL;

import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

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
            final BlackDuckServerConfig blackDuckServerConfig = hubServiceHelper.createBlackDuckServerConfig();
            final URL blackDuckURL = blackDuckServerConfig.getBlackDuckUrl();
            final String cliInstallRootDirectory = String.format("hub%s", String.valueOf(blackDuckURL.getHost().hashCode()));
            logger.info(String.format("CLI Installation Root Directory for %s: %s", blackDuckURL.toString(), cliInstallRootDirectory));

            final ArtifactScanner scanner = new ArtifactScanner(getEvent(), logger, getAttributeHelper(), hubServiceHelper);
            final ProjectVersionWrapper projectVersionWrapper = scanner.scan();
            if (projectVersionWrapper != null) {
                final ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();
                logger.info("Posting policy check event for " + projectVersionView.getVersionName());
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
