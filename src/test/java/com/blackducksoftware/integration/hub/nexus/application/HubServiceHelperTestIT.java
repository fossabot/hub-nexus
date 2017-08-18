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
package com.blackducksoftware.integration.hub.nexus.application;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.nexus.helpers.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.helpers.TestingPropertyKey;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.test.TestLogger;

public class HubServiceHelperTestIT {

    private final RestConnectionTestHelper restConnection = new RestConnectionTestHelper();

    private final HubServicesFactory hubServicesFactory;
    private final HubServiceHelper hubServiceHelper;
    private final Map<String, String> params;
    private final TestLogger logger = new TestLogger();

    public HubServiceHelperTestIT() throws HubIntegrationException, EncryptionException {
        params = generateParams();
        hubServicesFactory = restConnection.createHubServicesFactory();
        hubServiceHelper = new HubServiceHelper(logger, params);
    }

    private Map<String, String> generateParams() {
        final Map<String, String> newParams = new HashMap<>();

        newParams.put(TaskField.HUB_URL.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL));
        newParams.put(TaskField.HUB_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_USERNAME));
        newParams.put(TaskField.HUB_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PASSWORD));
        newParams.put(TaskField.HUB_TIMEOUT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT));
        newParams.put(TaskField.HUB_PROXY_HOST.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_HOST_BASIC));
        newParams.put(TaskField.HUB_PROXY_PORT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PORT_BASIC));
        newParams.put(TaskField.HUB_PROXY_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_USER_BASIC));
        newParams.put(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PASSWORD_BASIC));
        newParams.put(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_AUTO_IMPORT_HTTPS_CERT));

        return newParams;
    }

    @Test
    public void createHubServerConfig() throws EncryptionException {
        final HubServerConfig actual = hubServiceHelper.createHubServerConfig(params);
        final HubServerConfig expected = restConnection.getHubServerConfig();

        Assert.assertEquals(actual.getHubUrl(), expected.getHubUrl());
        Assert.assertEquals(actual.getGlobalCredentials(), expected.getGlobalCredentials());
        Assert.assertEquals(actual.getTimeout(), expected.getTimeout());
        Assert.assertEquals(actual.getProxyInfo(), expected.getProxyInfo());
        Assert.assertEquals(actual.isAutoImportHttpsCertificates(), expected.isAutoImportHttpsCertificates());

        // Assert.assertEquals(actual.getHubUrl(), "1");
        // Assert.assertEquals(actual.getGlobalCredentials().getUsername(), "2");
        // Assert.assertEquals(actual.getGlobalCredentials().getDecryptedPassword(), "3");
        // Assert.assertEquals(actual.getTimeout(), 4);
        // Assert.assertEquals(actual.getProxyInfo().getHost(), "5");
        // Assert.assertEquals(actual.getProxyInfo().getPort(), 6);
        // Assert.assertEquals(actual.getProxyInfo().getUsername(), "5");
        // Assert.assertEquals(actual.getProxyInfo().getDecryptedPassword(), "5");
        // Assert.assertEquals(actual.isAutoImportHttpsCertificates(), true);
    }

    // @Test
    public void waitForHubResponseTest() {

    }
}
