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
package com.blackducksoftware.integration.hub.nexus.scan;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.service.SignatureScannerService;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;

public class ArtifactScanner {
    private final boolean HUB_SCAN_DRY_RUN = false;
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
        final File workingDirectory = new File(scanInstallDirectory, event.getEventId().toString());
        try {
            logger.info("Beginning scan of artifact");
            if (hubServiceHelper == null) {
                logger.error("Hub Service Helper not initialized.  Unable to communicate with the configured hub server");
                attributesHelper.clearBlackduckAttributes(item);
                attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_FAILED);
                return null;
            } else {
                final String scanMemoryValue = getParameter(TaskField.HUB_SCAN_MEMORY.getParameterKey());
                final HubServerConfig hubServerConfig = hubServiceHelper.getHubServerConfig();
                final HubScanConfig scanConfig = createScanConfig(Integer.parseInt(scanMemoryValue), workingDirectory);
                logger.info(String.format("Scan Path %s", scanConfig.getScanTargetPaths()));
                final SignatureScannerService signatureScannerService = hubServiceHelper.getSignatureScannerService();
                final ProjectVersionWrapper projectVersionView = signatureScannerService.installAndRunControlledScan(hubServerConfig, scanConfig, event.getProjectRequest(), true);
                logger.info("Checking scan results...");
                final String apiUrl = hubServiceHelper.getHubResponseService().getHref(projectVersionView.getProjectVersionView());
                final String uiUrl = hubServiceHelper.getHubResponseService().getFirstLink(projectVersionView.getProjectVersionView(), "components");

                if (StringUtils.isNotBlank(apiUrl)) {
                    attributesHelper.setApiUrl(item, apiUrl);
                }

                if (StringUtils.isNotBlank(uiUrl)) {
                    attributesHelper.setUiUrl(item, uiUrl);
                }
                attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_SUCCESS);
                return projectVersionView.getProjectVersionView();
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scan task", ex);
            attributesHelper.clearBlackduckAttributes(item);
            attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_FAILED);
            return null;
        } finally {
            attributesHelper.setScanTime(item, System.currentTimeMillis());
            FileUtils.deleteQuietly(workingDirectory);
        }
    }

    private String getParameter(final String key) {
        return event.getTaskParameters().get(key);
    }

    private HubScanConfig createScanConfig(final int scanMemory, final File workingDirectory) throws IOException {
        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
        hubScanConfigBuilder.setScanMemory(scanMemory);
        hubScanConfigBuilder.setDryRun(HUB_SCAN_DRY_RUN);
        final File cliInstallDirectory = new File(scanInstallDirectory, "tools");
        hubScanConfigBuilder.setToolsDir(cliInstallDirectory);
        hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
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
