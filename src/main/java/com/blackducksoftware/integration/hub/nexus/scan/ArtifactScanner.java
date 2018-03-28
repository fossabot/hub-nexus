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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;

public class ArtifactScanner {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean HUB_SCAN_DRY_RUN = false;
    private final HubScanEvent event;
    private final ItemAttributesHelper attributesHelper;
    private final HubServiceHelper hubServiceHelper;
    private final File scanInstallDirectory;
    private final IntegrationInfo phoneHomeInfo;

    public ArtifactScanner(final HubScanEvent event, final ItemAttributesHelper attributesHelper, final File scanInstallDirectory, final HubServiceHelper hubserviceHelper, final IntegrationInfo phoneHomeInfo) {
        this.event = event;
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
                final CLIDataService cliDataService = hubServiceHelper.getCliDataService();
                final ProjectVersionView projectVersionView = cliDataService.installAndRunControlledScan(hubServerConfig, scanConfig, event.getProjectRequest(), true, phoneHomeInfo.getThirdPartyName(), phoneHomeInfo.getThirdPartyVersion(),
                        phoneHomeInfo.getPluginVersion());
                logger.info("Checking scan results...");
                final String apiUrl = hubServiceHelper.getMetaService().getHref(projectVersionView);
                final String uiUrl = hubServiceHelper.getMetaService().getFirstLink(projectVersionView, "components");

                if (StringUtils.isNotBlank(apiUrl)) {
                    attributesHelper.setApiUrl(item, apiUrl);
                }

                if (StringUtils.isNotBlank(uiUrl)) {
                    attributesHelper.setUiUrl(item, uiUrl);
                }
                attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_SUCCESS);
                return projectVersionView;
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
