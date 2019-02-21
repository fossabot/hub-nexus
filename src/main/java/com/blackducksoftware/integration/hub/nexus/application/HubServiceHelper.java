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
package com.blackducksoftware.integration.hub.nexus.application;

import java.util.Map;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.PolicyRuleService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.ReportService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class HubServiceHelper {
    private final IntLogger intLogger;
    private BlackDuckServerConfig hubServerConfig;
    private final Map<String, String> taskParameters;

    private BlackDuckServicesFactory hubServicesFactory;

    private BlackDuckService blackDuckService;
    private SignatureScannerService signatureScannerService;
    private PolicyRuleService policyRuleService;
    private ReportService reportService;
    private ProjectService projectService;

    public HubServiceHelper(final IntLogger logger, final Map<String, String> taskParameters) {
        this.intLogger = logger;
        this.taskParameters = taskParameters;
    }

    public BlackDuckServerConfig createHubServerConfig(final Map<String, String> taskParameters) {

        final String hubUrl = taskParameters.get(TaskField.HUB_URL.getParameterKey());
        final String hubUsername = taskParameters.get(TaskField.HUB_USERNAME.getParameterKey());
        final String hubPassword = taskParameters.get(TaskField.HUB_PASSWORD.getParameterKey());
        final String hubTimeout = taskParameters.get(TaskField.HUB_TIMEOUT.getParameterKey());
        final String proxyHost = taskParameters.get(TaskField.HUB_PROXY_HOST.getParameterKey());
        final String proxyPort = taskParameters.get(TaskField.HUB_PROXY_PORT.getParameterKey());
        final String proxyUsername = taskParameters.get(TaskField.HUB_PROXY_USERNAME.getParameterKey());
        final String proxyPassword = taskParameters.get(TaskField.HUB_PROXY_PASSWORD.getParameterKey());
        final String autoImport = taskParameters.get(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey());

        final BlackDuckServerConfigBuilder hubServerConfigBuilder = new BlackDuckServerConfigBuilder();
        hubServerConfigBuilder.setUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);
        hubServerConfigBuilder.setTimeout(hubTimeout);
        hubServerConfigBuilder.setProxyHost(proxyHost);
        hubServerConfigBuilder.setProxyPort(proxyPort);
        hubServerConfigBuilder.setProxyUsername(proxyUsername);
        hubServerConfigBuilder.setProxyPassword(proxyPassword);
        hubServerConfigBuilder.setTrustCert(autoImport);

        return hubServerConfigBuilder.build();
    }

    public BlackDuckServerConfig getHubServerConfig() {
        if (hubServerConfig == null) {
            hubServerConfig = createHubServerConfig(taskParameters);
        }
        return hubServerConfig;
    }

    public synchronized BlackDuckServicesFactory getHubServicesFactory() {
        if (hubServicesFactory == null) {
            setHubServicesFactory(getHubServerConfig().createBlackDuckServicesFactory(intLogger));
        }
        return hubServicesFactory;
    }

    public SignatureScannerService getSignatureScannerService() {
        if (signatureScannerService == null) {
            signatureScannerService = getHubServicesFactory().createSignatureScannerService();
        }
        return signatureScannerService;
    }

    public PolicyRuleService getPolicyRuleService() {
        if (policyRuleService == null) {
            policyRuleService = getHubServicesFactory().createPolicyRuleService();
        }
        return policyRuleService;
    }

    public ReportService getReportService(final long timeout) {
        if (reportService == null) {
            try {
                reportService = getHubServicesFactory().createReportService(timeout);
            } catch (final IntegrationException e) {
                e.printStackTrace();
                intLogger.error("Error retrieving risk report service");
            }
        }
        return reportService;
    }

    public ReportService getReportService() {
        return getReportService(getHubServerConfig().getTimeout());
    }

    public ProjectService getProjectService() {
        if (projectService == null) {
            projectService = getHubServicesFactory().createProjectService();
        }

        return projectService;
    }

    public BlackDuckService getBlackDuckService() {
        if (blackDuckService == null) {
            blackDuckService = getHubServicesFactory().createBlackDuckService();
        }

        return blackDuckService;
    }

    public void setHubServicesFactory(final BlackDuckServicesFactory hubServicesFactory) {
        this.hubServicesFactory = hubServicesFactory;
    }
}
