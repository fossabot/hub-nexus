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
package com.blackducksoftware.integration.hub.nexus.scan;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTaskDescriptor;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

public class ArtifactScanner {
    public static final String SCAN_CODE_LOCATION_NAME = "HubNexusScan";

    private final HubEventLogger logger;
    private final HubScanEvent event;
    private final ItemAttributesHelper attributesHelper;
    private final HubServiceHelper hubServiceHelper;

    public ArtifactScanner(final HubScanEvent event, final HubEventLogger logger, final ItemAttributesHelper attributesHelper, final HubServiceHelper hubserviceHelper) {
        this.event = event;
        this.logger = logger;
        this.attributesHelper = attributesHelper;
        this.hubServiceHelper = hubserviceHelper;
    }

    public ProjectVersionWrapper scan() {
        final StorageItem item = event.getItem();
        final BlackDuckServerConfig blackDuckServerConfig = hubServiceHelper.createBlackDuckServerConfig();
        final File cliInstallDirectory = getSignatureScannerInstallDirectory(blackDuckServerConfig);
        final File outputDirectory = getSignatureScannerOutputDirectory(cliInstallDirectory, event.getEventId().toString());
        try {
            logger.info("Beginning scan of artifact");
            if (hubServiceHelper == null) {
                logger.error("Hub Service Helper not initialized.  Unable to communicate with the configured hub server");
                attributesHelper.clearBlackduckAttributes(item);
                attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_FAILED);
                return null;
            } else {
                final String scanMemoryValue = getParameter(TaskField.HUB_SCAN_MEMORY.getParameterKey());
                final String projectName = event.getProjectVersionWrapper().getProjectView().getName();
                final String projectVersion = event.getProjectVersionWrapper().getProjectVersionView().getVersionName();
                final String codeLocationName = String.join("/", SCAN_CODE_LOCATION_NAME, event.getRepository().getName(), projectName, projectVersion);
                final ScanBatch scanBatch = createScanBatch(blackDuckServerConfig, Integer.parseInt(scanMemoryValue), cliInstallDirectory, outputDirectory, projectName,
                    projectVersion, codeLocationName);

                final String targets = StringUtils.join(scanBatch.getScanTargets(), ", ");
                logger.info(String.format("Scan Path %s", targets));

                final SignatureScannerService signatureScannerService = hubServiceHelper.createBlackDuckServicesFactory().createSignatureScannerService();
                final ScanBatchOutput scanBatchOutput = signatureScannerService.performSignatureScanAndWait(scanBatch, blackDuckServerConfig.getTimeout() * 5);

                logger.info("Checking scan results...");
                final ScanCommandOutput scanCommandOutput = scanBatchOutput.getOutputs()
                                                                .stream()
                                                                .findFirst()
                                                                .orElse(null);

                if (null != scanCommandOutput) {
                    if (Result.SUCCESS == scanCommandOutput.getResult()) {
                        final ProjectVersionView projectVersionView = event.getProjectVersionWrapper().getProjectVersionView();
                        final String apiUrl = projectVersionView.getHref().orElse("");
                        final String uiUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK).orElse("");

                        if (StringUtils.isNotBlank(apiUrl)) {
                            attributesHelper.setApiUrl(item, apiUrl);
                        }
                        if (StringUtils.isNotBlank(uiUrl)) {
                            attributesHelper.setUiUrl(item, uiUrl);
                        }
                        attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_SUCCESS);
                        return event.getProjectVersionWrapper();
                    }
                    logger.error(String.format("Error occurred scanning %s: %s", projectName, scanCommandOutput.getErrorMessage().orElse("")), scanCommandOutput.getException().orElse(null));
                }
                attributesHelper.clearBlackduckAttributes(item);
                attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_FAILED);
                return null;
            }
        } catch (final Exception ex) {
            logger.error("Error occurred during scan task", ex);
            attributesHelper.clearBlackduckAttributes(item);
            attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_FAILED);
            return null;
        } finally {
            attributesHelper.setScanTime(item, System.currentTimeMillis());
            FileUtils.deleteQuietly(outputDirectory);
        }
    }

    private String getParameter(final String key) {
        return event.getTaskParameters().get(key);
    }

    private ScanBatch createScanBatch(final BlackDuckServerConfig blackDuckServerConfig, final int scanMemory, final File scanInstallDirectory, final File outputDirectory, final String projectName, final String projectVersion,
        final String codeLocationName)
        throws IOException {
        final ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();
        scanBatchBuilder.fromBlackDuckServerConfig(blackDuckServerConfig);
        scanBatchBuilder.installDirectory(scanInstallDirectory);
        scanBatchBuilder.outputDirectory(outputDirectory);
        scanBatchBuilder.projectAndVersionNames(projectName, projectVersion);

        final Repository repository = event.getRepository();
        final StorageItem item = event.getItem();
        final ResourceStoreRequest request = event.getRequest();
        final DefaultFSLocalRepositoryStorage storage = (DefaultFSLocalRepositoryStorage) repository.getLocalStorage();
        final File repositoryPath = storage.getFileFromBase(repository, request);
        final File file = new File(repositoryPath, item.getPath());

        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(file.getCanonicalPath(), codeLocationName));
        scanBatchBuilder.scanMemoryInMegabytes(scanMemory);

        return scanBatchBuilder.build();
    }

    private File getSignatureScannerInstallDirectory(final BlackDuckServerConfig blackDuckServerConfig) {
        final File blackDuckDirectory = new File(getParameter(TaskField.WORKING_DIRECTORY.getParameterKey()), ScanTaskDescriptor.BLACKDUCK_DIRECTORY);
        final String cliInstallRootDirectory = String.format("hub%s", String.valueOf(blackDuckServerConfig.getBlackDuckUrl().getHost().hashCode()));
        final File taskDirectory = new File(blackDuckDirectory, cliInstallRootDirectory);
        final File cliInstallDirectory = new File(taskDirectory, "tools");
        if (!cliInstallDirectory.exists()) {
            cliInstallDirectory.mkdirs();
        }
        return cliInstallDirectory;
    }

    private File getSignatureScannerOutputDirectory(final File installDirectory, final String identifier) {
        final File outputDirectory = new File(installDirectory, "output");
        final File uniqueOutputDirectory = new File(outputDirectory, identifier);
        if (!uniqueOutputDirectory.exists()) {
            uniqueOutputDirectory.mkdirs();
        }
        return outputDirectory;
    }
}
