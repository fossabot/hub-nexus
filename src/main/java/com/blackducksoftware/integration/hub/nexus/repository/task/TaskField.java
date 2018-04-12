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
package com.blackducksoftware.integration.hub.nexus.repository.task;

public enum TaskField {
    DISTRIBUTION("blackduck.hub.project.version.distribution"),
    FILE_PATTERNS("blackduck.hub.nexus.file.pattern.match.wildcards"),
    HUB_AUTO_IMPORT_CERT("blackduck.hub.auto.import.cert"),
    HUB_PASSWORD("blackduck.hub.password"),
    HUB_PROXY_HOST("blackduck.hub.proxy.host"),
    HUB_PROXY_PORT("blackduck.hub.proxy.port"),
    HUB_PROXY_USERNAME("blackduck.hub.proxy.username"),
    HUB_PROXY_PASSWORD("blackduck.hub.proxy.password"),
    HUB_SCAN_MEMORY("blackduck.hub.scan.memory"),
    HUB_TIMEOUT("blackduck.hub.timeout"),
    HUB_USERNAME("blackduck.hub.username"),
    HUB_URL("blackduck.hub.url"),
    PHASE("blackduck.hub.project.version.phase"),
    REPOSITORY_FIELD_ID("repositoryId"),
    REPOSITORY_PATH_FIELD_ID("repositoryPath"),
    WORKING_DIRECTORY("blackduck.hub.nexus.working.directory"),
    OLD_ARTIFACT_CUTOFF("blackduck.hub.nexus.artifact.cutoff"),
    RESCAN_FAILURES("blackduck.hub.nexus.rescan.failures"),
    ALWAYS_SCAN("blackduck.hub.nexus.rescan.always"),
    MAX_SCANS("blackduck.hub.nexus.max.scans"),
    CURRENT_SCANS("blackduck.hub.nexus.current.scans"),
    MAX_POLICY_CHECKS("blackduck.hub.nexus.max.policy.check"),
    CURRENT_POLICY_CHECKS("blackduck.hub.nexus.current.policy.check");

    private String parameterKey;

    private TaskField(final String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public String getParameterKey() {
        return this.parameterKey;
    }
}
