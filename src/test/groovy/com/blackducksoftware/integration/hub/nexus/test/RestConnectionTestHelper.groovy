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
package com.blackducksoftware.integration.hub.nexus.test

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger

public class RestConnectionTestHelper {
    private Properties testProperties

    private final String hubServerUrl

    public RestConnectionTestHelper() {
        initProperties()
        this.hubServerUrl = getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL)
    }

    public RestConnectionTestHelper(final String hubServerUrlPropertyName) {
        initProperties()
        this.hubServerUrl = testProperties.getProperty(hubServerUrlPropertyName)
    }

    private void initProperties() {
        testProperties = new Properties()
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        try {
            final InputStream is = classLoader.getResourceAsStream("test.properties")
            testProperties.load(is)
        } catch (final Exception e) {
            System.err.println("reading test.properties failed!")
        }

        if (testProperties.isEmpty()) {
            try {
                loadOverrideProperties(TestingPropertyKey.values())
            } catch (final Exception e) {
                System.err.println("reading properties from the environment failed")
            }
        }
    }

    private void loadOverrideProperties(final TestingPropertyKey[] keys) {
        for (final TestingPropertyKey key : keys) {
            final String prop = System.getenv(key.toString())
            if (prop != null && !prop.isEmpty()) {
                testProperties.setProperty(key.toString(), prop)
            }
        }
    }

    public String getProperty(final TestingPropertyKey key) {
        return getProperty(key.toString())
    }

    public String getProperty(final String key) {
        return testProperties.getProperty(key)
    }

    public BlackDuckServerConfig getBlackDuckServerConfig() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder()
        builder.setUrl(hubServerUrl)
        builder.setUsername(getProperty(TestingPropertyKey.TEST_USERNAME))
        builder.setPassword(getProperty(TestingPropertyKey.TEST_PASSWORD))
        builder.setTimeout(getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT))
        builder.setProxyHost(getProperty(TestingPropertyKey.TEST_PROXY_HOST_BASIC))
        builder.setProxyPort(getProperty(TestingPropertyKey.TEST_PROXY_PORT_BASIC))
        builder.setProxyUsername(getProperty(TestingPropertyKey.TEST_PROXY_USER_BASIC))
        builder.setProxyPassword(getProperty(TestingPropertyKey.TEST_PROXY_PASSWORD_BASIC))
        boolean autoImportHttpsCertificates = Boolean.parseBoolean(getProperty(TestingPropertyKey.TEST_AUTO_IMPORT_HTTPS_CERT))
        builder.setTrustCert(autoImportHttpsCertificates)

        return builder.build()
    }

    public String getIntegrationHubServerUrl() {
        return getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL)
    }

    public String getTestUsername() {
        return getProperty(TestingPropertyKey.TEST_USERNAME)
    }

    public String getTestPassword() {
        return getProperty(TestingPropertyKey.TEST_PASSWORD)
    }


    public BlackDuckServicesFactory createBlackDuckServicesFactory() throws IllegalArgumentException {
        return createHubServicesFactory(LogLevel.TRACE)
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(final LogLevel logLevel) throws IllegalArgumentException {
        return createHubServicesFactory(new PrintStreamIntLogger(System.out, logLevel))
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(final IntLogger logger) throws IllegalArgumentException {
        return getBlackDuckServerConfig().createBlackDuckServicesFactory(logger)
    }

    public File getFile(final String classpathResource) {
        try {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(classpathResource)
            final File file = new File(url.toURI().getPath())
            return file
        } catch (final Exception e) {
            fail("Could not get file: " + e.getMessage())
            return null
        }
    }
}
