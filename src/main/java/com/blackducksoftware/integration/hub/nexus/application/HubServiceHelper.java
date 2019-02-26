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
package com.blackducksoftware.integration.hub.nexus.application;

import java.util.Map;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;

public class HubServiceHelper {
    private final IntLogger intLogger;
    private final Map<String, String> taskParameters;

    public HubServiceHelper(final IntLogger logger, final Map<String, String> taskParameters) {
        this.intLogger = logger;
        this.taskParameters = taskParameters;
    }

    public BlackDuckServerConfig createBlackDuckServerConfig() {
        final String hubUrl = taskParameters.get(TaskField.HUB_URL.getParameterKey());
        final String hubUsername = taskParameters.get(TaskField.HUB_USERNAME.getParameterKey());
        final String hubPassword = taskParameters.get(TaskField.HUB_PASSWORD.getParameterKey());
        final String hubTimeout = taskParameters.get(TaskField.HUB_TIMEOUT.getParameterKey());
        final String proxyHost = taskParameters.get(TaskField.HUB_PROXY_HOST.getParameterKey());
        final String proxyPort = taskParameters.get(TaskField.HUB_PROXY_PORT.getParameterKey());
        final String proxyUsername = taskParameters.get(TaskField.HUB_PROXY_USERNAME.getParameterKey());
        final String proxyPassword = taskParameters.get(TaskField.HUB_PROXY_PASSWORD.getParameterKey());
        final String autoImport = taskParameters.get(TaskField.HUB_TRUST_CERT.getParameterKey());

        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(hubUrl);
        blackDuckServerConfigBuilder.setUsername(hubUsername);
        blackDuckServerConfigBuilder.setPassword(hubPassword);
        blackDuckServerConfigBuilder.setTimeout(hubTimeout);
        blackDuckServerConfigBuilder.setProxyHost(proxyHost);
        blackDuckServerConfigBuilder.setProxyPort(proxyPort);
        blackDuckServerConfigBuilder.setProxyUsername(proxyUsername);
        blackDuckServerConfigBuilder.setProxyPassword(proxyPassword);
        blackDuckServerConfigBuilder.setTrustCert(autoImport);

        return blackDuckServerConfigBuilder.build();
    }

    public synchronized BlackDuckServicesFactory createBlackDuckServicesFactory() {
        return createBlackDuckServerConfig().createBlackDuckServicesFactory(intLogger);
    }

}
