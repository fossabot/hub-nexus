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
package com.blackducksoftware.integration.hub.nexus.scan;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.report.api.ReportData;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class HubServiceHelper {
    private final Logger logger = Loggers.getLogger(ArtifactScanner.class);
    private final IntLogger intLogger = new Slf4jIntLogger(logger);

    private HubServicesFactory hubServicesFactory;

    public HubServiceHelper(final HubServerConfig hubServerConfig) {
        CredentialsRestConnection credentialsRestConnection;
        try {
            credentialsRestConnection = hubServerConfig.createCredentialsRestConnection(intLogger);
            hubServicesFactory = new HubServicesFactory(credentialsRestConnection);
        } catch (final EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitForHubResponse(final ProjectVersionView version, final long timeout) {
        logger.info("Waiting for hub response");
        try {
            final List<CodeLocationView> allCodeLocations = hubServicesFactory.createCodeLocationRequestService(intLogger).getAllCodeLocationsForProjectVersion(version);
            logger.info("Checking policy of + " + allCodeLocations.size() + " code location's");
            final List<ScanSummaryView> scanSummaryViews = new ArrayList<>();
            for (final CodeLocationView codeLocationView : allCodeLocations) {
                final String scansLink = hubServicesFactory.createMetaService(intLogger).getFirstLinkSafely(codeLocationView, MetaService.SCANS_LINK);
                final List<ScanSummaryView> codeLocationScanSummaryViews = hubServicesFactory.createScanSummaryRequestService().getAllScanSummaryItems(scansLink);
                scanSummaryViews.addAll(codeLocationScanSummaryViews);
            }
            logger.info("Checking scan policy");
            hubServicesFactory.createScanStatusDataService(intLogger, timeout).assertScansFinished(scanSummaryViews);
            logger.info("Policy check completed");
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
        }
    }

    public PolicyStatusDescription checkPolicyStatus(final ProjectVersionView version) {
        logger.info("Checking policy status");
        try {
            final VersionBomPolicyStatusView versionBomPolicyStatusView = hubServicesFactory.createPolicyStatusDataService(intLogger).getPolicyStatusForVersion(version);
            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(versionBomPolicyStatusView);
            return policyStatusDescription;
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
        }
    }

    public ReportData retrieveRiskReport(final long timeout, final ProjectVersionView version, final ProjectView project) {
        logger.info("Generating risk report");
        try {
            final RiskReportDataService riskReport = hubServicesFactory.createRiskReportDataService(intLogger, timeout);
            return riskReport.getRiskReportData(project, version);
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
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
}
