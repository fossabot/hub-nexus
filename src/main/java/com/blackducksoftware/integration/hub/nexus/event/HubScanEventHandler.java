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
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTaskDescriptor;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner;
import com.blackducksoftware.integration.hub.nexus.scan.HubServiceHelper;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class HubScanEventHandler extends HubEventHandler {
    private final ApplicationConfiguration appConfiguration;
    private final EventBus eventBus;

    @Inject
    public HubScanEventHandler(final ApplicationConfiguration appConfiguration, final EventBus eventBus, final DefaultAttributesHandler attributesHandler) {
        super(attributesHandler);
        this.appConfiguration = appConfiguration;
        this.eventBus = eventBus;
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle(final HubScanEvent event) {
        File blackDuckDirectory = null;
        try {
            blackDuckDirectory = new File(event.getTaskParameters().get(TaskField.WORKING_DIRECTORY.getParameterKey()), "blackduck");
            if (!blackDuckDirectory.exists()) {
                blackDuckDirectory.mkdirs();
            }
            log.info("Scan Event Handler called for event {}", event);
            final IntegrationInfo phoneHomeInfo = new IntegrationInfo(ThirdPartyName.NEXUS, appConfiguration.getConfigurationModel().getNexusVersion(), ScanTaskDescriptor.PLUGIN_VERSION);
            final HubServerConfig hubServerConfig = createHubServerConfig(event.getTaskParameters());
            final HubServiceHelper hubServiceHelper = createServiceHelper(hubServerConfig);
            final ArtifactScanner scanner = new ArtifactScanner(createHubServerConfig(event.getTaskParameters()), event.getRepository(), event.getRequest(), event.getItem(), getAttributeHelper(), blackDuckDirectory,
                    event.getTaskParameters(), hubServiceHelper, phoneHomeInfo);
            final ProjectVersionView projectVersionView = scanner.scan();
            if (projectVersionView != null) {
                eventBus.post(new HubPolicyCheckEvent(event.getRepository(), event.getItem(), event.getTaskParameters(), event.getRequest(), projectVersionView));
            }
        } catch (final Exception ex) {
            log.error("Error occurred during scanning of event {} ", event, ex);
        } finally {
            try {
                if (blackDuckDirectory != null) {
                    FileUtils.deleteDirectory(blackDuckDirectory);
                }
            } catch (final IOException ioex) {
                log.error("Error deleting blackduck working directory", ioex);
            }
        }
    }
}
