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
package com.blackducksoftware.integration.hub.nexus.repository.walker;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.nexus.scan.ArtifactScanner;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class RepositoryWalker extends AbstractWalkerProcessor {
    private final HubServerConfig hubServerConfig;
    private final Logger logger = Loggers.getLogger(getClass());
    private final String fileMatchPatterns;
    private final ItemAttributesHelper attributesHelper;
    private final File blackDuckDirectory;
    private final Map<String, String> taskParameters;

    public RepositoryWalker(final HubServerConfig hubServerConfig, final String fileMatchPatterns, final ItemAttributesHelper attributesHelper, final File blackDuckDirectory, final Map<String, String> taskParameters) {
        this.hubServerConfig = hubServerConfig;
        this.fileMatchPatterns = fileMatchPatterns;
        this.attributesHelper = attributesHelper;
        this.blackDuckDirectory = blackDuckDirectory;
        this.taskParameters = taskParameters;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        try {
            if (item instanceof StorageCollectionItem) {
                return; // directory found
            }
            if (item.getRepositoryItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)) {
                return;
            }

            if (StringUtils.isNotBlank(item.getRemoteUrl())) {
                logger.info("Item came from a proxied repository skipping: {}", item);
                return;
            }

            final String[] patternArray = StringUtils.split(fileMatchPatterns, ",");
            for (final String wildCardPattern : patternArray) {
                if (FilenameUtils.wildcardMatch(item.getPath(), wildCardPattern)) {
                    logger.debug("Repository id: {}", context.getRepository().getId());
                    logger.debug("Item repository id: {}", item.getRepositoryId());
                    logger.debug("Item repository UID: {}", item.getRepositoryItemUid());
                    logger.debug("Evaluating item: {}", item);
                    final long lastScanned = attributesHelper.getAttributeLastScanned(item);
                    logger.debug("Last scanned " + lastScanned + " ms ago");
                    final long lastModified = item.getRepositoryItemAttributes().getModified();
                    logger.debug("Last modified " + lastModified + " ms ago");
                    if (lastScanned > lastModified) {
                        logger.info(item.getName() + " already scanned");
                        return;
                    }
                    final ArtifactScanner scanner = new ArtifactScanner(hubServerConfig, context.getRepository(), context.getResourceStoreRequest(), item, attributesHelper, blackDuckDirectory, taskParameters);
                    scanner.scan();
                    break;
                }
            }
        } catch (final Exception ex) {
            logger.error("Error occurred in walker processor for repository: ", ex);
        }
    }
}
