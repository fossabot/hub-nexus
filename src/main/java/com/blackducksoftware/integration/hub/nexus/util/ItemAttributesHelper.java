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
package com.blackducksoftware.integration.hub.nexus.util;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.sisu.goodies.common.Loggers;

@Named
@Singleton
public class ItemAttributesHelper {
    public static final long SCAN_STATUS_FAILED = 0;
    public static final long SCAN_STATUS_SUCCESS = 1;
    public static final String BLACK_DUCK_SCAN_TIME_PROPERTY_NAME = "scanTime";
    public static final String BLACK_DUCK_SCAN_RESULT_PROPERTY_NAME = "scanResult";
    public static final String BLACK_DUCK_PROJECT_VERSION_URL_PROPERTY_NAME = "apiUrl";
    public static final String BLACK_DUCK_PROJECT_VERSION_UI_URL_PROPERTY_NAME = "uiUrl";
    public static final String BLACK_DUCK_POLICY_STATUS_PROPERTY_NAME = "policyStatus";
    public static final String BLACK_DUCK_OVERALL_POLICY_STATUS_PROPERTY_NAME = "overallPolicyStatus";
    public static final String BLACKDUCK_PREFIX = "blackduck-";
    private final AttributesHandler attributesHandler;
    private final Logger logger = Loggers.getLogger(getClass());

    @Inject
    public ItemAttributesHelper(final AttributesHandler attributesHandler) {
        this.attributesHandler = attributesHandler;
    }

    private String keyName(final String key) {
        return BLACKDUCK_PREFIX.concat(key);
    }

    public void addAttribute(final String key, final String value, final StorageItem item) {
        item.getRepositoryItemAttributes().put(keyName(key), value);
        try {
            attributesHandler.storeAttributes(item);
        } catch (final IOException e) {
            logger.error("AttributesHandler error when adding error", e);
        }
    }

    private long getLong(final StorageItem item, final String key, final long defaultValue) {
        if (item.getRepositoryItemAttributes().containsKey(keyName(key))) {
            return Long.parseLong(item.getRepositoryItemAttributes().get(keyName(key)));
        }
        return defaultValue;
    }

    private String getString(final StorageItem item, final String key, final String defaultValue) {
        if (item.getRepositoryItemAttributes().containsKey(keyName(key))) {
            return item.getRepositoryItemAttributes().get(keyName(key));
        }
        return defaultValue;
    }

    public boolean contains(String key, final StorageItem item) {
        final Attributes attList = item.getRepositoryItemAttributes();
        if (!key.contains(BLACKDUCK_PREFIX)) {
            key = keyName(key);
        }
        if (attList.containsKey(key)) {
            return true;
        }
        return false;
    }

    public void clearBlackduckAttributes(final StorageItem item) {
        final Attributes attList = item.getRepositoryItemAttributes();
        final Set<String> keys = attList.asMap().keySet();

        for (final String key : keys) {
            if (key.startsWith(BLACKDUCK_PREFIX)) {
                logger.debug("Removing key " + key);
                attList.remove(key);
            }
        }

        try {
            attributesHandler.storeAttributes(item);
        } catch (final IOException e) {
            logger.error("AttributesHandler error when clearing attributes");
        }
    }

    public long getScanTime(final StorageItem item) {
        return getLong(item, BLACK_DUCK_SCAN_TIME_PROPERTY_NAME, 0);
    }

    public void setScanTime(final StorageItem item, final long newTime) {
        final String timeString = String.valueOf(newTime);
        addAttribute(BLACK_DUCK_SCAN_TIME_PROPERTY_NAME, timeString, item);
    }

    public long getScanResult(final StorageItem item) {
        return getLong(item, BLACK_DUCK_SCAN_RESULT_PROPERTY_NAME, SCAN_STATUS_FAILED);
    }

    public void setScanResult(final StorageItem item, final long scanResult) {
        final String statusString = String.valueOf(scanResult);
        addAttribute(BLACK_DUCK_SCAN_RESULT_PROPERTY_NAME, statusString, item);
    }

    public String getPolicyStatus(final StorageItem item) {
        return getString(item, BLACK_DUCK_POLICY_STATUS_PROPERTY_NAME, "");
    }

    public void setPolicyStatus(final StorageItem item, final String newResult) {
        addAttribute(BLACK_DUCK_POLICY_STATUS_PROPERTY_NAME, newResult, item);
    }

    public String getOverallPolicyStatus(final StorageItem item) {
        return getString(item, BLACK_DUCK_OVERALL_POLICY_STATUS_PROPERTY_NAME, "");
    }

    public void setOverallPolicyStatus(final StorageItem item, final String overallPolicyStatus) {
        addAttribute(BLACK_DUCK_OVERALL_POLICY_STATUS_PROPERTY_NAME, overallPolicyStatus, item);
    }

    public String getApiUrl(final StorageItem item) {
        return getString(item, BLACK_DUCK_PROJECT_VERSION_URL_PROPERTY_NAME, "");
    }

    public void setApiUrl(final StorageItem item, final String url) {
        addAttribute(BLACK_DUCK_PROJECT_VERSION_URL_PROPERTY_NAME, url, item);
    }

    public String getUiUrl(final StorageItem item) {
        return getString(item, BLACK_DUCK_PROJECT_VERSION_UI_URL_PROPERTY_NAME, "");
    }

    public void setUiUrl(final StorageItem item, final String url) {
        addAttribute(BLACK_DUCK_PROJECT_VERSION_UI_URL_PROPERTY_NAME, url, item);
    }
}
