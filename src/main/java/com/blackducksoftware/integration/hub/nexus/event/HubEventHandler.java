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

import java.util.Map;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class HubEventHandler extends ComponentSupport implements EventSubscriber {
    private final ItemAttributesHelper attributeHelper;

    public HubEventHandler(final DefaultAttributesHandler attributesHandler) {
        this.attributeHelper = new ItemAttributesHelper(attributesHandler);
    }

    public ItemAttributesHelper getAttributeHelper() {
        return attributeHelper;
    }

    public HubServerConfig createHubServerConfig(final Map<String, String> taskParameters) {

        final String hubUrl = taskParameters.get(TaskField.HUB_URL.getParameterKey());
        final String hubUsername = taskParameters.get(TaskField.HUB_USERNAME.getParameterKey());
        final String hubPassword = taskParameters.get(TaskField.HUB_PASSWORD.getParameterKey());
        final String hubTimeout = taskParameters.get(TaskField.HUB_TIMEOUT.getParameterKey());
        final String proxyHost = taskParameters.get(TaskField.HUB_PROXY_HOST.getParameterKey());
        final String proxyPort = taskParameters.get(TaskField.HUB_PROXY_PORT.getParameterKey());
        final String proxyUsername = taskParameters.get(TaskField.HUB_PROXY_USERNAME.getParameterKey());
        final String proxyPassword = taskParameters.get(TaskField.HUB_PROXY_PASSWORD.getParameterKey());
        final String autoImport = taskParameters.get(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey());

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);
        hubServerConfigBuilder.setTimeout(hubTimeout);
        hubServerConfigBuilder.setProxyHost(proxyHost);
        hubServerConfigBuilder.setProxyPort(proxyPort);
        hubServerConfigBuilder.setProxyUsername(proxyUsername);
        hubServerConfigBuilder.setProxyPassword(proxyPassword);
        hubServerConfigBuilder.setAutoImportHttpsCertificates(Boolean.parseBoolean(autoImport));

        return hubServerConfigBuilder.build();
    }

    public HubServiceHelper createServiceHelper(final HubEventLogger logger, final HubServerConfig hubServerConfig) {
        try {
            return new HubServiceHelper(logger, hubServerConfig);
        } catch (final EncryptionException ex) {
            return null;
        }
    }
}
