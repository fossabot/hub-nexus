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
package com.blackducksoftware.integration.hub.nexus.repository.task.filter;

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class ScanRepositoryWalkerFilter extends RepositoryWalkerFilter {
    private final String fileMatchPatterns;
    private final Map<String, String> taskParameters;

    public ScanRepositoryWalkerFilter(final String fileMatchPatterns, final ItemAttributesHelper itemAttributesHelper, final Map<String, String> taskParameters) {
        super(itemAttributesHelper);
        this.fileMatchPatterns = fileMatchPatterns;
        this.taskParameters = taskParameters;
    }

    @Override
    public boolean shouldProcess(final WalkerContext context, final StorageItem item) {
        return super.shouldProcess(context, item) && shouldScan(item);
    }

    private boolean shouldScan(final StorageItem item) {
        final String[] patternArray = StringUtils.split(fileMatchPatterns, ",");
        for (final String wildCardPattern : patternArray) {
            if (FilenameUtils.wildcardMatch(item.getPath(), wildCardPattern)) {
                if (isArtifactTooOld(item)) {
                    logger.info("Item is older than specified age, skipping: {}", item);
                    return false;
                }

                if (isModifiedSinceScan(item)) {
                    final String alwaysScanString = taskParameters.get(TaskField.ALWAYS_SCAN.getParameterKey());
                    final boolean alwaysScan = Boolean.parseBoolean(alwaysScanString);
                    return alwaysScan || wasScanSuccessful(item);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean wasScanSuccessful(final StorageItem item) {
        final long scanResult = itemAttributesHelper.getScanResult(item);
        logger.debug("Previous scan result {}", scanResult);
        if (scanResult == ItemAttributesHelper.SCAN_STATUS_FAILED) {
            final String rescanFailure = taskParameters.get(TaskField.RESCAN_FAILURES.getParameterKey());
            final boolean performRescan = Boolean.parseBoolean(rescanFailure);
            if (performRescan) {
                logger.debug("{} already scanned but re-scan failed option selected.", item.getName());
                return true;
            } else {
                logger.debug("{} already scanned, but failed.  Configure the re-scan option on the task to scan again.", item.getName());
                return false;
            }
        } else {
            logger.debug("{} already scanned successfully", item.getName());
            return false;
        }
    }

    private boolean isModifiedSinceScan(final StorageItem item) {
        logger.debug("Evaluating item: {}", item);
        final long scanTime = itemAttributesHelper.getScanTime(item);
        logger.debug("Last scanned " + scanTime + " ms ago");
        final long lastModified = item.getRepositoryItemAttributes().getModified();
        logger.debug("Last modified " + lastModified + " ms ago");

        return scanTime > lastModified;
    }

    private boolean isArtifactTooOld(final StorageItem item) {
        final String cutoffDate = taskParameters.get(TaskField.OLD_ARTIFACT_CUTOFF.getParameterKey());

        if (cutoffDate == null) {
            return false;
        }

        final long cutoffTime = getTimeFromString(cutoffDate);
        final long modifiedTime = item.getModified();

        return modifiedTime < cutoffTime;
    }

    private long getTimeFromString(final String dateTimeString) {
        final String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        return DateTime.parse(dateTimeString, DateTimeFormat.forPattern(dateTimePattern).withZoneUTC()).toDate().getTime();
    }

}
