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
package com.blackducksoftware.integration.hub.nexus.http;

import org.slf4j.Logger;
import org.sonatype.sisu.goodies.common.Loggers;

//@XmlType(name = "blackduck-info")
//@XmlAccessorType(XmlAccessType.FIELD)
public class HubMetaData {
    final Logger logger = Loggers.getLogger(HubMetaData.class);

    private String apiUrl;
    private String policyStatus;
    private String policyOverallStatus;
    private String scanTime;
    private String scanStatus;
    private String uiUrl;

    public HubMetaData() {
        logger.info("HubMetaData");
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(final String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(final String policyStatus) {
        this.policyStatus = policyStatus;
    }

    public String getPolicyOverallStatus() {
        return policyOverallStatus;
    }

    public void setPolicyOverallStatus(final String policyOverallStatus) {
        this.policyOverallStatus = policyOverallStatus;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(final String scanTime) {
        this.scanTime = scanTime;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(final String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public String getUiUrl() {
        return uiUrl;
    }

    public void setUiUrl(final String uiUrl) {
        this.uiUrl = uiUrl;
    }

    @Override
    public String toString() {
        return "HubMetaData [logger=" + logger + ", apiUrl=" + apiUrl + ", policyStatus=" + policyStatus + ", policyOverallStatus=" + policyOverallStatus + ", scanTime=" + scanTime + ", scanStatus=" + scanStatus + ", uiUrl=" + uiUrl + "]";
    }
}
