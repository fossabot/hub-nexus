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
package com.blackducksoftware.integration.hub.nexus.application;

import java.util.Map;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.SignatureScannerService;
import com.blackducksoftware.integration.log.IntLogger;

public class HubServiceHelper {
    private final IntLogger intLogger;
    private HubServerConfig hubServerConfig;
    private final Map<String, String> taskParameters;

    private HubServicesFactory hubServicesFactory;

    private ProjectService projectRequestService;
    private HubService hubResponseService;
    private SignatureScannerService signatureScannerService;
    private CLIDownloadUtility cliDownloadUtility;

    public HubServiceHelper(final IntLogger logger, final Map<String, String> taskParameters) {
        this.intLogger = logger;
        this.taskParameters = taskParameters;
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
        hubServerConfigBuilder.setAlwaysTrustServerCertificate(Boolean.parseBoolean(autoImport));

        return hubServerConfigBuilder.build();
    }

    public HubServerConfig getHubServerConfig() {
        if (hubServerConfig == null) {
            hubServerConfig = createHubServerConfig(taskParameters);
        }

        return hubServerConfig;
    }

    public HubServicesFactory getHubServicesFactory() {
        if (hubServicesFactory == null) {
            CredentialsRestConnection credentialsRestConnection = null;

            try {
                credentialsRestConnection = getHubServerConfig().createCredentialsRestConnection(intLogger);
            } catch (final EncryptionException e) {
                intLogger.error("Encryption error when creating REST connection");
                e.printStackTrace();
            }
            setHubServicesFactory(new HubServicesFactory(credentialsRestConnection));
        }

        return hubServicesFactory;
    }

    public ProjectService getProjectRequestService() {
        if (projectRequestService == null) {
            projectRequestService = getHubServicesFactory().createProjectService();
        }

        return projectRequestService;
    }

    public HubService getHubResponseService() {
        if (hubResponseService == null) {
            hubResponseService = getHubServicesFactory().createHubService();
        }

        return hubResponseService;
    }

    public SignatureScannerService getSignatureScannerService() {
        if (signatureScannerService == null) {
            signatureScannerService = getHubServicesFactory().createSignatureScannerService();
        }

        return signatureScannerService;
    }

    public CLIDownloadUtility getCliDownloadUtility() {
        if (cliDownloadUtility == null) {
            cliDownloadUtility = getHubServicesFactory().createCliDownloadUtility();
        }

        return cliDownloadUtility;
    }

    public void setHubServicesFactory(final HubServicesFactory hubServicesFactory) {
        this.hubServicesFactory = hubServicesFactory;
    }
}
