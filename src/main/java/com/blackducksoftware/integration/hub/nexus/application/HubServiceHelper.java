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
package com.blackducksoftware.integration.hub.nexus.application;

import java.util.Map;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;

public class HubServiceHelper {
    private final IntLogger intLogger;
    private HubServerConfig hubServerConfig;
    private final Map<String, String> taskParameters;

    private HubServicesFactory hubServicesFactory;

    private PolicyStatusDataService policyStatusDataService;
    private MetaService metaService;
    private RiskReportDataService riskReportDataService;
    private CLIDataService cliDataService;
    private ProjectRequestService projectRequestService;
    private HubResponseService hubResponseService;
    private ProjectVersionRequestService projectVersionRequestService;
    private CLIDownloadService cliDownloadService;
    private HubVersionRequestService hubVersionRequestService;

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
        hubServerConfigBuilder.setAutoImportHttpsCertificates(Boolean.parseBoolean(autoImport));

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
                credentialsRestConnection = createHubServerConfig(taskParameters).createCredentialsRestConnection(intLogger);
            } catch (final EncryptionException e) {
                e.printStackTrace();
                intLogger.error("Encryption error when creating REST connection");
            }
            setHubServicesFactory(new HubServicesFactory(credentialsRestConnection));
        }

        return hubServicesFactory;
    }

    public PolicyStatusDataService getPolicyStatusDataService() {
        if (policyStatusDataService == null) {
            policyStatusDataService = getHubServicesFactory().createPolicyStatusDataService(intLogger);
        }

        return policyStatusDataService;
    }

    public MetaService getMetaService() {
        if (metaService == null) {
            metaService = getHubServicesFactory().createMetaService(intLogger);
        }

        return metaService;
    }

    public RiskReportDataService getRiskReportDataService(final long timeout) {
        if (riskReportDataService == null) {
            try {
                riskReportDataService = getHubServicesFactory().createRiskReportDataService(intLogger, timeout);
            } catch (final IntegrationException e) {
                e.printStackTrace();
                intLogger.error("Error retrieving risk report service");
            }
        }

        return riskReportDataService;
    }

    public RiskReportDataService getRiskReportDataService() {
        return getRiskReportDataService(getHubServerConfig().getTimeout());
    }

    public CLIDataService getCliDataService() {
        if (cliDataService == null) {
            cliDataService = getHubServicesFactory().createCLIDataService(intLogger);
        }

        return cliDataService;
    }

    public ProjectRequestService getProjectRequestService() {
        if (projectRequestService == null) {
            projectRequestService = getHubServicesFactory().createProjectRequestService(intLogger);
        }

        return projectRequestService;
    }

    public HubResponseService getHubResponseService() {
        if (hubResponseService == null) {
            hubResponseService = getHubServicesFactory().createHubResponseService();
        }

        return hubResponseService;
    }

    public ProjectVersionRequestService getProjectVersionRequestService() {
        if (projectVersionRequestService == null) {
            projectVersionRequestService = getHubServicesFactory().createProjectVersionRequestService(intLogger);
        }

        return projectVersionRequestService;
    }

    public CLIDownloadService getCliDownloadService() {
        if (cliDownloadService == null) {
            cliDownloadService = getHubServicesFactory().createCliDownloadService(intLogger);
        }

        return cliDownloadService;
    }

    public HubVersionRequestService getHubVersionRequestService() {
        if (hubVersionRequestService == null) {
            hubVersionRequestService = getHubServicesFactory().createHubVersionRequestService();
        }

        return hubVersionRequestService;
    }

    public void setHubServicesFactory(final HubServicesFactory hubServicesFactory) {
        this.hubServicesFactory = hubServicesFactory;
    }
}
