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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;

public class ArtifactScanner {
    final Logger logger = Loggers.getLogger(ArtifactScanner.class);
    final boolean HUB_SCAN_DRY_RUN = false;

    private final HubServerConfig hubServerConfig;
    private final StorageItem item;
    private final Repository repository;
    private final ResourceStoreRequest request;
    private final ItemAttributesHelper attributesHelper;
    private HubServiceHelper hubServiceHelper;
    private final File blackDuckDirectory;
    private final Map<String, String> taskParameters;
    private final IntegrationInfo phoneHomeInfo;

    public ArtifactScanner(final HubServerConfig hubServerConfig, final Repository repository, final ResourceStoreRequest request, final StorageItem item, final ItemAttributesHelper attributesHelper, final File blackDuckDirectory,
            final Map<String, String> taskParameters, final IntegrationInfo phoneHomeInfo) {
        this.hubServerConfig = hubServerConfig;
        this.repository = repository;
        this.item = item;
        this.request = request;
        this.attributesHelper = attributesHelper;
        this.blackDuckDirectory = blackDuckDirectory;
        this.taskParameters = taskParameters;
        this.phoneHomeInfo = phoneHomeInfo;
        try {
            hubServiceHelper = new HubServiceHelper(hubServerConfig);
        } catch (final EncryptionException ex) {
            logger.error("HubServiceHelper couldn't be instantiated", ex);
            hubServiceHelper = null;
        }
    }

    public void scan() {
        try {
            logger.info("Beginning scan of artifact");
            if (hubServiceHelper == null) {
                logger.error("Hub Service Helper not initialized.  Unable to communicate with the configured hub server");
            } else {
                final String scanMemoryValue = taskParameters.get(TaskField.HUB_SCAN_MEMORY.getParameterKey());
                final HubScanConfig scanConfig = createScanConfig(Integer.parseInt(scanMemoryValue));
                logger.info("Scan Path {}", scanConfig.getScanTargetPaths());
                final CLIDataService cliDataService = hubServiceHelper.createCLIDataService();
                final String distribution = taskParameters.get(TaskField.DISTRIBUTION.getParameterKey());
                final String phase = taskParameters.get(TaskField.PHASE.getParameterKey());
                final ProjectRequest projectRequest = createProjectRequest(distribution, phase);
                final ProjectVersionView projectVersionView = cliDataService.installAndRunControlledScan(hubServerConfig, scanConfig, projectRequest, true, phoneHomeInfo);
                attributesHelper.setScanTime(item, System.currentTimeMillis());
                logger.info("Checking scan results...");
                hubServiceHelper.waitForHubResponse(projectVersionView, hubServerConfig.getTimeout());
                final String apiUrl = hubServiceHelper.retrieveApiUrl(projectVersionView);
                final String uiUrl = hubServiceHelper.retrieveUIUrl(projectVersionView);
                if (StringUtils.isNotBlank(apiUrl)) {
                    attributesHelper.setApiUrl(item, apiUrl);
                }

                if (StringUtils.isNotBlank(uiUrl)) {
                    attributesHelper.setUiUrl(item, uiUrl);
                }
                populatePolicyStatus(projectVersionView);
                attributesHelper.setScanResult(item, "SUCCESS");
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scan task", ex);
            attributesHelper.clearAttributes(item);
        }
    }

    private void populatePolicyStatus(final ProjectVersionView projectVersionView) throws IntegrationException {
        final PolicyStatusDescription policyCheckResults = hubServiceHelper.checkPolicyStatus(projectVersionView);
        if (policyCheckResults != null) {
            final VersionBomPolicyStatusView versionBomPolicyStatusView = hubServiceHelper.getOverallPolicyStatus(projectVersionView);
            attributesHelper.setPolicyStatus(item, policyCheckResults.getPolicyStatusMessage());
            attributesHelper.setOverallPolicyStatus(item, versionBomPolicyStatusView.overallStatus.toString());
        }
    }

    private ProjectRequest createProjectRequest(final String distribution, final String phase) {
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

    private HubScanConfig createScanConfig(final int scanMemory) throws IOException {
        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
        hubScanConfigBuilder.setScanMemory(scanMemory);
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
