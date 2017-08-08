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
package com.blackducksoftware.integration.hub.nexus.util;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;

@Named
@Singleton
public class ItemAttributesHelper {
    public static final String LAST_SCANNED = "lastScanned";
    public static final String POLICY_CHECK_RESULT = "policyResult";
    public static final String RISK_REPORT_URL = "riskReportUrl";

    private final DefaultAttributesHandler attributesHandler;

    @Inject
    public ItemAttributesHelper(final DefaultAttributesHandler attributesHandler) {
        this.attributesHandler = attributesHandler;
    }

    private void addAttribute(final String key, final String value, final StorageItem item) {
        item.getRepositoryItemAttributes().put(key, value);
        try {
            attributesHandler.storeAttributes(item);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getLong(final StorageItem item, final String key, final long defaultValue) {
        if (item.getRepositoryItemAttributes().containsKey(key)) {
            return Long.parseLong(item.getRepositoryItemAttributes().get(key));
        }
        return defaultValue;
    }

    private String getString(final StorageItem item, final String key, final String defaultValue) {
        if (item.getRepositoryItemAttributes().containsKey(key)) {
            return item.getRepositoryItemAttributes().get(key);
        }
        return defaultValue;
    }

    public long getAttributeLastScanned(final StorageItem item) {
        return getLong(item, LAST_SCANNED, 0);
    }

    public void setAttributeLastScanned(final StorageItem item, final long newTime) {
        final String timeString = String.valueOf(newTime);
        addAttribute(LAST_SCANNED, timeString, item);
    }

    public String getAttributePolicyResult(final StorageItem item) {
        return getString(item, POLICY_CHECK_RESULT, "");
    }

    public void setAttributePolicyResult(final StorageItem item, final String newResult) {
        addAttribute(POLICY_CHECK_RESULT, newResult, item);
    }

    public String getAttributeRiskReportUrl(final StorageItem item) {
        return getString(item, RISK_REPORT_URL, "");
    }

    public void setAttributeRiskReportUrl(final StorageItem item, final String url) {
        addAttribute(RISK_REPORT_URL, url, item);
    }

}
