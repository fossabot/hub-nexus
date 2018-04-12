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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;

public class ScanAttributesHelper {
    Map<String, String> scanAttributes;

    public ScanAttributesHelper(final Map<String, String> scanAttributes) {
        this.scanAttributes = new ConcurrentHashMap<>(scanAttributes);
    }

    public final int getIntegerAttribute(final String key) {
        final String value = scanAttributes.get(key);

        if (value != null && !StringUtils.isEmpty(value)) {
            return Integer.parseInt(value);
        }

        return 0;
    }

    public final boolean getBooleanAttribute(final String key) {
        final String value = scanAttributes.get(key);
        return Boolean.parseBoolean(value);
    }

    public final String getStringAttribute(final String key) {
        return scanAttributes.get(key);
    }

    public final int getIntegerAttribute(final TaskField key) {
        return getIntegerAttribute(key.getParameterKey());
    }

    public final boolean getBooleanAttribute(final TaskField key) {
        return getBooleanAttribute(key.getParameterKey());
    }

    public final String getStringAttribute(final TaskField key) {
        return getStringAttribute(key.getParameterKey());
    }

    public final Map<String, String> getScanAttributes() {
        return scanAttributes;
    }

    public void setScanAttributes(final Map<String, String> scanAttributes) {
        this.scanAttributes = scanAttributes;
    }

    public final int getCurrentScans() {
        return getIntegerAttribute(TaskField.CURRENT_SCANS);
    }

    public final void setCurrentScans(final int value) {
        scanAttributes.put(TaskField.CURRENT_SCANS.getParameterKey(), String.valueOf(value));
    }

    public final int getCurrentPolicyChecks() {
        return getIntegerAttribute(TaskField.CURRENT_POLICY_CHECKS);
    }

    public final void setCurrentPolicyChecks(final int value) {
        scanAttributes.put(TaskField.CURRENT_POLICY_CHECKS.getParameterKey(), String.valueOf(value));
    }

}
