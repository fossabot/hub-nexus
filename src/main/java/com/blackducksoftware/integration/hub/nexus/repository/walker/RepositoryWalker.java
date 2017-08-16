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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class RepositoryWalker extends AbstractWalkerProcessor {
    private final Logger logger = Loggers.getLogger(getClass());
    private final String fileMatchPatterns;
    private final ItemAttributesHelper attributesHelper;
    private final Map<String, String> taskParameters;
    private final EventBus eventBus;

    public RepositoryWalker(final String fileMatchPatterns, final ItemAttributesHelper attributesHelper, final Map<String, String> taskParameters, final EventBus eventBus) {
        this.fileMatchPatterns = fileMatchPatterns;
        this.attributesHelper = attributesHelper;
        this.taskParameters = taskParameters;
        this.eventBus = eventBus;
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
                logger.info("Item came from a proxied repository, skipping: {}", item);
                return;
            }

            final String[] patternArray = StringUtils.split(fileMatchPatterns, ",");
            for (final String wildCardPattern : patternArray) {
                if (FilenameUtils.wildcardMatch(item.getPath(), wildCardPattern)) {
                    if (isArtifactTooOld(item)) {
                        logger.info("Item is older than specified date, skipping: {}", item);
                        return;
                    }

                    logger.debug("Evaluating item: {}", item);
                    final long scanTime = attributesHelper.getScanTime(item);
                    logger.debug("Last scanned " + scanTime + " ms ago");
                    final long lastModified = item.getRepositoryItemAttributes().getModified();
                    logger.debug("Last modified " + lastModified + " ms ago");
                    if (scanTime > lastModified) {
                        logger.info(item.getName() + " already scanned");
                        return;
                    }
                    eventBus.post(new HubScanEvent(item.getRepositoryItemUid().getRepository(), item, taskParameters, context.getResourceStoreRequest()));
                    break;
                }
            }
        } catch (final Exception ex) {
            logger.error("Error occurred in walker processor for repository: ", ex);
        }
    }

    private boolean isArtifactTooOld(final StorageItem item) {
        final int daysCutoff = Integer.parseInt(taskParameters.get(TaskField.OLD_ARTIFACT_CUTOFF.getParameterKey()));
        final long createdTime = item.getCreated();
        logger.info("Created time: " + createdTime);

        final long daysInMill = TimeUnit.MILLISECONDS.convert(daysCutoff, TimeUnit.DAYS);
        logger.info("Days in Mill: " + daysInMill);
        final long cutoffTime = System.currentTimeMillis() - daysInMill;
        logger.info("Cutoff time: " + cutoffTime);

        if (createdTime < cutoffTime) {
            return true;
        }

        return false;
    }
}
