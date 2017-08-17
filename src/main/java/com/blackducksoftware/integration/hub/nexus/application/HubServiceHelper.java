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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.item.StorageItem;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubTimeoutExceededException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.scan.NameVersionNode;
import com.blackducksoftware.integration.hub.report.api.ReportData;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class HubServiceHelper {
    private final IntLogger intLogger;

    private final HubServicesFactory hubServicesFactory;
    private final MetaService metaService;
    private final HubServerConfig hubServerConfig;

    public HubServiceHelper(final IntLogger logger, final Map<String, String> taskParameters) throws EncryptionException {
        this.hubServerConfig = createHubServerConfig(taskParameters);
        this.intLogger = logger;
        CredentialsRestConnection credentialsRestConnection;
        credentialsRestConnection = hubServerConfig.createCredentialsRestConnection(intLogger);
        hubServicesFactory = new HubServicesFactory(credentialsRestConnection);
        metaService = hubServicesFactory.createMetaService(intLogger);
    }

    public HubServerConfig getHubServerConfig() {
        return hubServerConfig;
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

    public void waitForHubResponse(final ProjectVersionView version, final long timeout) throws HubTimeoutExceededException, IntegrationException {
        intLogger.info("Waiting for hub response");
        final List<CodeLocationView> allCodeLocations = hubServicesFactory.createCodeLocationRequestService(intLogger).getAllCodeLocationsForProjectVersion(version);
        intLogger.info("Waiting for + " + allCodeLocations.size() + " code location's");
        final List<ScanSummaryView> scanSummaryViews = new ArrayList<>();
        for (final CodeLocationView codeLocationView : allCodeLocations) {
            final String scansLink = hubServicesFactory.createMetaService(intLogger).getFirstLinkSafely(codeLocationView, MetaService.SCANS_LINK);
            final List<ScanSummaryView> codeLocationScanSummaryViews = hubServicesFactory.createScanSummaryRequestService().getAllScanSummaryItems(scansLink);
            scanSummaryViews.addAll(codeLocationScanSummaryViews);
        }
        intLogger.info("Checking scan finished");
        hubServicesFactory.createScanStatusDataService(intLogger, timeout).assertScansFinished(scanSummaryViews);
        intLogger.info("Scan finished ");

    }

    public PolicyStatusDescription checkPolicyStatus(final ProjectVersionView version) {
        try {
            final VersionBomPolicyStatusView versionBomPolicyStatusView = hubServicesFactory.createPolicyStatusDataService(intLogger).getPolicyStatusForVersion(version);
            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(versionBomPolicyStatusView);
            return policyStatusDescription;
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
        }
    }

    public String retrieveApiUrl(final ProjectVersionView project) {
        try {
            return metaService.getHref(project);
        } catch (final HubIntegrationException e) {
            return "";
        }
    }

    public String retrieveUIUrl(final ProjectVersionView project) {
        try {
            return metaService.getFirstLink(project, "components");
        } catch (final HubIntegrationException e) {
            return "";
        }
    }

    public ReportData retrieveRiskReport(final long timeout, final ProjectVersionView version, final ProjectView project) {
        intLogger.info("Generating risk report");
        try {
            final RiskReportDataService riskReport = hubServicesFactory.createRiskReportDataService(intLogger, timeout);
            return riskReport.getRiskReportData(project, version);
        } catch (final IntegrationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CLIDataService createCLIDataService() {
        return hubServicesFactory.createCLIDataService(intLogger);
    }

    public ProjectView getProjectView(final String projectName) {
        final ProjectRequestService requestService = hubServicesFactory.createProjectRequestService(intLogger);
        try {
            return requestService.getProjectByName(projectName);
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
        }
    }

    public VersionBomPolicyStatusView getOverallPolicyStatus(final ProjectVersionView projectVersionView) throws IntegrationException {
        final String policyStatusUrl = metaService.getFirstLink(projectVersionView, MetaService.POLICY_STATUS_LINK);
        final HubResponseService hubResponseService = hubServicesFactory.createHubResponseService();
        final VersionBomPolicyStatusView versionBomPolicyStatusView = hubResponseService.getItem(policyStatusUrl, VersionBomPolicyStatusView.class);

        return versionBomPolicyStatusView;
    }

    public ProjectRequest createProjectRequest(final String distribution, final String phase, final StorageItem item) {
        final ProjectRequestBuilder builder = new ProjectRequestBuilder();
        final NameVersionNode nameVersionGuess = generateProjectNameVersion(item);
        builder.setProjectName(nameVersionGuess.getName());
        builder.setVersionName(nameVersionGuess.getVersion());
        builder.setProjectLevelAdjustments(true);
        builder.setPhase(phase);
        builder.setDistribution(distribution);
        return builder.build();
    }

    // TODO Check item att for name and version (More options)
    private NameVersionNode generateProjectNameVersion(final StorageItem item) {
        final String path = item.getParentPath();
        String name = item.getName();
        String version = "0.0.0";
        final String[] pathSections = path.split("/");
        if (pathSections.length > 1) {
            version = pathSections[pathSections.length - 1];
            name = pathSections[pathSections.length - 2];
        }
        final NameVersionNode nameVersion = new NameVersionNode(name, version);
        return nameVersion;
    }

    public void createProjectAndVersion(final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null;
        final ProjectRequestService projectRequestService = hubServicesFactory.createProjectRequestService(intLogger);
        final ProjectVersionRequestService projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService(intLogger);
        try {
            project = projectRequestService.getProjectByName(projectRequest.getName());
        } catch (final DoesNotExistException e) {
            final String projectURL = projectRequestService.createHubProject(projectRequest);
            project = projectRequestService.getItem(projectURL, ProjectView.class);
        }
        try {
            projectVersionRequestService.getProjectVersion(project, projectRequest.getVersionRequest().getVersionName());
        } catch (final DoesNotExistException e) {
            final String versionURL = projectVersionRequestService.createHubVersion(project, projectRequest.getVersionRequest());
            projectVersionRequestService.getItem(versionURL, ProjectVersionView.class);
        }
    }

    public void installCLI(final File installDirectory) throws IntegrationException {
        final String localHostName = HostnameHelper.getMyHostname();
        intLogger.info("Installing CLI to the following location: " + localHostName + ": " + installDirectory);
        final CIEnvironmentVariables ciEnvironmentVariables = new CIEnvironmentVariables();
        ciEnvironmentVariables.putAll(System.getenv());
        final HubVersionRequestService hubVersionRequestService = hubServicesFactory.createHubVersionRequestService();
        final CLIDownloadService cliDownloadService = hubServicesFactory.createCliDownloadService(intLogger);
        final String hubVersion = hubVersionRequestService.getHubVersion();
        cliDownloadService.performInstallation(installDirectory, ciEnvironmentVariables, hubServerConfig.getHubUrl().toString(), hubVersion, localHostName);
    }
}
