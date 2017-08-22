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

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;

public class ArtifactScanner {
    final boolean HUB_SCAN_DRY_RUN = false;

    private final HubEventLogger logger;
    private final HubScanEvent event;
    private final ItemAttributesHelper attributesHelper;
    private final HubServiceHelper hubServiceHelper;
    private final File scanInstallDirectory;
    private final IntegrationInfo phoneHomeInfo;

    public ArtifactScanner(final HubScanEvent event, final HubEventLogger logger, final ItemAttributesHelper attributesHelper, final File scanInstallDirectory, final HubServiceHelper hubserviceHelper, final IntegrationInfo phoneHomeInfo) {
        this.event = event;
        this.logger = logger;
        this.attributesHelper = attributesHelper;
        this.scanInstallDirectory = scanInstallDirectory;
        this.phoneHomeInfo = phoneHomeInfo;
        this.hubServiceHelper = hubserviceHelper;
    }

    public ProjectVersionView scan() {
        final StorageItem item = event.getItem();
        try {
            logger.info("Beginning scan of artifact");
            if (event.isProcessed()) {
                logger.info("Scan event already processed");
            }
            if (hubServiceHelper == null) {
                logger.error("Hub Service Helper not initialized.  Unable to communicate with the configured hub server");
            } else {
                final String scanMemoryValue = getParameter(TaskField.HUB_SCAN_MEMORY.getParameterKey());
                final HubServerConfig hubServerConfig = hubServiceHelper.getHubServerConfig();
                final HubScanConfig scanConfig = createScanConfig(Integer.parseInt(scanMemoryValue));
                logger.info(String.format("Scan Path %s", scanConfig.getScanTargetPaths()));
                final CLIDataService cliDataService = hubServiceHelper.createCLIDataService();
                final String distribution = getParameter(TaskField.DISTRIBUTION.getParameterKey());
                final String phase = getParameter(TaskField.PHASE.getParameterKey());
                final ProjectRequest projectRequest = hubServiceHelper.createProjectRequest(distribution, phase, event.getItem());
                final ProjectVersionView projectVersionView = cliDataService.installAndRunControlledScan(hubServerConfig, scanConfig, projectRequest, true, phoneHomeInfo);
                if (!event.isProcessed()) {
                    attributesHelper.setScanTime(item, System.currentTimeMillis());
                    logger.info("Checking scan results...");
                    final long timeoutInMilliseconds = hubServerConfig.getTimeout() * 1000;
                    hubServiceHelper.waitForHubResponse(projectVersionView, timeoutInMilliseconds);
                    final String apiUrl = hubServiceHelper.retrieveApiUrl(projectVersionView);
                    final String uiUrl = hubServiceHelper.retrieveUIUrl(projectVersionView);

                    if (StringUtils.isNotBlank(apiUrl)) {
                        attributesHelper.setApiUrl(item, apiUrl);
                    }

                    if (StringUtils.isNotBlank(uiUrl)) {
                        attributesHelper.setUiUrl(item, uiUrl);
                    }
                    attributesHelper.setScanResult(item, "SUCCESS");
                }

                return projectVersionView;
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scan task", ex);
            attributesHelper.clearAttributes(item);
            attributesHelper.setScanResult(item, "FAILURE");
        }
        return null;
    }

    private String getParameter(final String key) {
        return event.getTaskParameters().get(key);
    }

    private HubScanConfig createScanConfig(final int scanMemory) throws IOException {
        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
        hubScanConfigBuilder.setScanMemory(scanMemory);
        hubScanConfigBuilder.setDryRun(HUB_SCAN_DRY_RUN);
        final File cliInstallDirectory = new File(scanInstallDirectory, "tools");
        hubScanConfigBuilder.setToolsDir(cliInstallDirectory);
        hubScanConfigBuilder.setWorkingDirectory(this.scanInstallDirectory);
        hubScanConfigBuilder.disableScanTargetPathExistenceCheck();

        final Repository repository = event.getRepository();
        final StorageItem item = event.getItem();
        final ResourceStoreRequest request = event.getRequest();
        final DefaultFSLocalRepositoryStorage storage = (DefaultFSLocalRepositoryStorage) repository.getLocalStorage();
        final File repositoryPath = storage.getFileFromBase(repository, request);
        final File file = new File(repositoryPath, item.getPath());
        hubScanConfigBuilder.addScanTargetPath(file.getCanonicalPath());

        return hubScanConfigBuilder.build();
    }
}
