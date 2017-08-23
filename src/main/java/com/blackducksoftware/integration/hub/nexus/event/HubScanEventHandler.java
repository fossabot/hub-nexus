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
package com.blackducksoftware.integration.hub.nexus.event;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTaskDescriptor;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class HubScanEventHandler extends HubEventHandler {
    private final ApplicationConfiguration appConfiguration;
    private final EventBus eventBus;
    private final ScanEventManager eventManager;

    @Inject
    public HubScanEventHandler(final ApplicationConfiguration appConfiguration, final EventBus eventBus, final DefaultAttributesHandler attributesHandler, final ScanEventManager eventManager) {
        super(attributesHandler);
        this.appConfiguration = appConfiguration;
        this.eventBus = eventBus;
        this.eventManager = eventManager;
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle(final HubScanEvent event) {
        final HubEventLogger logger = new HubEventLogger(event, LoggerFactory.getLogger(getClass()));
        try {
            logger.info("Begin handling scan event");
            if (event.isProcessed()) {
                logger.info("Event already processed.");
            } else {
                final IntegrationInfo phoneHomeInfo = new IntegrationInfo(ThirdPartyName.NEXUS, appConfiguration.getConfigurationModel().getNexusVersion(), ScanTaskDescriptor.PLUGIN_VERSION);
                final HubServiceHelper hubServiceHelper = createServiceHelper(logger, event.getTaskParameters());
                final String cliInstallRootDirectory = hubServiceHelper.createCLIInstallDirectoryName();
                final File blackDuckDirectory = new File(event.getTaskParameters().get(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
                final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);
                final ArtifactScanner scanner = new ArtifactScanner(event, logger, getAttributeHelper(), taskDirectory, hubServiceHelper, phoneHomeInfo);
                final ProjectVersionView projectVersionView = scanner.scan();
                if (projectVersionView != null) {
                    logger.info("Posting policy check event for " + projectVersionView.versionName);
                    eventBus.post(new HubPolicyCheckEvent(event.getRepository(), event.getItem(), event.getTaskParameters(), event.getRequest(), projectVersionView));
                }
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scanning", ex);
        } finally {
            eventManager.markScanEventProcessed(event);
            logger.info("Finished handling scan event");
        }
    }
}
