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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.report.api.ReportData;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.util.ProjectNameVersionGuess;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class ArtifactScanner {
    final Logger logger = Loggers.getLogger(ArtifactScanner.class);
    final int HUB_SCAN_MEMORY = 4096;
    final boolean HUB_SCAN_DRY_RUN = false;

    private final HubServerConfig hubServerConfig;
    private final StorageItem item;
    private final Repository repository;
    private final ResourceStoreRequest request;
    private final ItemAttributesHelper attributesHelper;
    private final HubServiceHelper hubServiceHelper;
    private final File blackDuckDirectory;

    private final IntLogger intLogger = new Slf4jIntLogger(logger);

    public ArtifactScanner(final HubServerConfig hubServerConfig, final Repository repository, final ResourceStoreRequest request, final StorageItem item, final ItemAttributesHelper attributesHelper, final File blackDuckDirectory) {
        this.hubServerConfig = hubServerConfig;
        this.repository = repository;
        this.item = item;
        this.request = request;
        this.attributesHelper = attributesHelper;
        this.blackDuckDirectory = blackDuckDirectory;
        hubServiceHelper = new HubServiceHelper(hubServerConfig);
    }

    public void scan() {
        try {
            logger.info("Beginning scan of artifact");
            final HubScanConfig scanConfig = createScanConfig();
            logger.info(String.format("Scan Path %s", scanConfig.getScanTargetPaths()));
            final CLIDataService cliDataService = hubServiceHelper.createCLIDataService();
            final ProjectRequest projectRequest = createProjectRequest();
            // TODO: Fix file paths. do not perform the scan the file paths do not exist causes scan to run in the hub for a long time.
            final ProjectVersionView projectVersionView = cliDataService.installAndRunControlledScan(hubServerConfig, scanConfig, projectRequest, true, IntegrationInfo.DO_NOT_PHONE_HOME);
            attributesHelper.setAttributeLastScanned(item, System.currentTimeMillis());
            logger.info("Checking scan results...");
            final ProjectView projectView = hubServiceHelper.getProjectView(projectRequest.getName());
            hubServiceHelper.waitForHubResponse(projectVersionView, 60000);
            final PolicyStatusDescription policyCheckResults = hubServiceHelper.checkPolicyStatus(projectVersionView);
            final ReportData riskReport = hubServiceHelper.retrieveRiskReport(60000, projectVersionView, projectView);
            if (policyCheckResults != null) {
                attributesHelper.setAttributePolicyResult(item, policyCheckResults.getPolicyStatusMessage());
            }
            if (riskReport != null) {
                attributesHelper.setAttributeRiskReportUrl(item, riskReport.getProjectURL());
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scan task", ex);
            // TODO: clean up any attributes associated with the scan
        }
    }

    private ProjectRequest createProjectRequest() {
        final ProjectRequestBuilder builder = new ProjectRequestBuilder();
        final ProjectNameVersionGuess nameVersionGuess = generateProjectNameVersion(item);
        builder.setProjectName(nameVersionGuess.getProjectName());
        builder.setVersionName(nameVersionGuess.getVersionName());
        builder.setProjectLevelAdjustments(true);
        builder.setPhase("Development");
        builder.setDistribution("External");
        return builder.build();
    }

    private ProjectNameVersionGuess generateProjectNameVersion(final StorageItem item) {
        final String path = item.getParentPath();
        String name = item.getName();
        String version = "0.0.0";
        final String[] pathSections = path.split("/");
        if (pathSections.length > 1) {
            version = pathSections[pathSections.length - 1];
            name = pathSections[pathSections.length - 2];
        }
        final ProjectNameVersionGuess nameVersion = new ProjectNameVersionGuess(name, version);
        return nameVersion;
    }

    private HubScanConfig createScanConfig() throws IOException {
        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
        hubScanConfigBuilder.setScanMemory(HUB_SCAN_MEMORY);
        hubScanConfigBuilder.setDryRun(HUB_SCAN_DRY_RUN);
        final File cliInstallDirectory = new File(blackDuckDirectory, "tools");
        hubScanConfigBuilder.setToolsDir(cliInstallDirectory);
        hubScanConfigBuilder.setWorkingDirectory(this.blackDuckDirectory);
        hubScanConfigBuilder.disableScanTargetPathExistenceCheck();

        final DefaultFSLocalRepositoryStorage storage = (DefaultFSLocalRepositoryStorage) repository.getLocalStorage();
        final File repositoryPath = storage.getFileFromBase(repository, request);
        final File file = new File(repositoryPath, item.getPath());
        hubScanConfigBuilder.addScanTargetPath(file.getCanonicalPath());

        return hubScanConfigBuilder.build();
    }
}
