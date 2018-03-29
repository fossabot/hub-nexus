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
package com.blackducksoftware.integration.hub.nexus.repository.task.walker;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class ScanRepositoryMarkerWalker extends AbstractWalkerProcessor {
    protected final Logger logger = Loggers.getLogger(getClass());

    private final ItemAttributesHelper attributesHelper;
    private final Map<String, String> params;

    public ScanRepositoryMarkerWalker(final ItemAttributesHelper attributesHelper, final Map<String, String> params) {
        this.attributesHelper = attributesHelper;
        this.params = params;
    }

    @Override
    public void processItem(final WalkerContext walkerContext, final StorageItem item) {
        boolean shouldProcess = true;
        final String currentScansString = params.get(TaskField.CURRENT_SCANS.getParameterKey());
        int currentScans = Integer.parseInt(currentScansString);
        final String maxScansString = params.get(TaskField.MAX_SCANS.getParameterKey());
        if (!StringUtils.isEmpty(maxScansString)) {
            final int maxScans = Integer.parseInt(maxScansString);
            shouldProcess = currentScans < maxScans;
        }

        if (shouldProcess) {
            logger.info("Set item to pending {}", item);
            attributesHelper.setScanResult(item, ItemAttributesHelper.SCAN_STATUS_PENDING);
            currentScans++;
            params.put(TaskField.CURRENT_SCANS.getParameterKey(), String.valueOf(currentScans));
        }
    }

}
