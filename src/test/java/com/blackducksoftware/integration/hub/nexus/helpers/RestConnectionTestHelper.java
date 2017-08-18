/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.nexus.helpers;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

import okhttp3.OkHttpClient;

public class RestConnectionTestHelper {
    private Properties testProperties;

    private final String hubServerUrl;

    public RestConnectionTestHelper() {
        initProperties();
        this.hubServerUrl = getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL);
    }

    public RestConnectionTestHelper(final String hubServerUrlPropertyName) {
        initProperties();
        this.hubServerUrl = testProperties.getProperty(hubServerUrlPropertyName);
    }

    private void initProperties() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        testProperties = new Properties();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = classLoader.getResourceAsStream("test.properties")) {
            testProperties.load(is);
        } catch (final Exception e) {
            System.err.println("reading test.properties failed!");
        }

        if (testProperties.isEmpty()) {
            try {
                loadOverrideProperties(TestingPropertyKey.values());
            } catch (final Exception e) {
                System.err.println("reading properties from the environment failed");
            }
        }
    }

    private void loadOverrideProperties(final TestingPropertyKey[] keys) {
        for (final TestingPropertyKey key : keys) {
            final String prop = System.getenv(key.toString());
            if (prop != null && !prop.isEmpty()) {
                testProperties.setProperty(key.toString(), prop);
            }
        }
    }

    public String getProperty(final TestingPropertyKey key) {
        return getProperty(key.toString());
    }

    public String getProperty(final String key) {
        return testProperties.getProperty(key);
    }

    public HubServerConfig getHubServerConfig() {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl(hubServerUrl);
        builder.setUsername(getProperty(TestingPropertyKey.TEST_USERNAME));
        builder.setPassword(getProperty(TestingPropertyKey.TEST_PASSWORD));
        builder.setTimeout(getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT));
        builder.setProxyHost(getProperty(TestingPropertyKey.TEST_PROXY_HOST_BASIC));
        builder.setProxyPort(getProperty(TestingPropertyKey.TEST_PROXY_PORT_BASIC));
        builder.setProxyUsername(getProperty(TestingPropertyKey.TEST_PROXY_USER_BASIC));
        builder.setProxyPassword(getProperty(TestingPropertyKey.TEST_PROXY_PASSWORD_BASIC));
        builder.setAutoImportHttpsCertificates(Boolean.getBoolean(getProperty(TestingPropertyKey.TEST_AUTO_IMPORT_HTTPS_CERT)));

        return builder.build();
    }

    public String getIntegrationHubServerUrl() {
        return getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL);
    }

    public String getTestUsername() {
        return getProperty(TestingPropertyKey.TEST_USERNAME);
    }

    public String getTestPassword() {
        return getProperty(TestingPropertyKey.TEST_PASSWORD);
    }

    public CredentialsRestConnection getIntegrationHubRestConnection() throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return getRestConnection(getHubServerConfig());
    }

    public CredentialsRestConnection getRestConnection(final HubServerConfig serverConfig) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return getRestConnection(serverConfig, LogLevel.TRACE);
    }

    public CredentialsRestConnection getRestConnection(final HubServerConfig serverConfig, final LogLevel logLevel) throws IllegalArgumentException, EncryptionException, HubIntegrationException {

        final CredentialsRestConnection restConnection = new CredentialsRestConnection(new PrintStreamIntLogger(System.out, logLevel), serverConfig.getHubUrl(), serverConfig.getGlobalCredentials().getUsername(),
                serverConfig.getGlobalCredentials().getDecryptedPassword(), serverConfig.getTimeout());
        restConnection.proxyHost = serverConfig.getProxyInfo().getHost();
        restConnection.proxyPort = serverConfig.getProxyInfo().getPort();
        restConnection.proxyNoHosts = serverConfig.getProxyInfo().getIgnoredProxyHosts();
        restConnection.proxyUsername = serverConfig.getProxyInfo().getUsername();
        restConnection.proxyPassword = serverConfig.getProxyInfo().getDecryptedPassword();

        return restConnection;
    }

    public HubServicesFactory createHubServicesFactory() throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return createHubServicesFactory(LogLevel.TRACE);
    }

    public HubServicesFactory createHubServicesFactory(final LogLevel logLevel) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return createHubServicesFactory(new PrintStreamIntLogger(System.out, logLevel));
    }

    public HubServicesFactory createHubServicesFactory(final IntLogger logger) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        final RestConnection restConnection = getIntegrationHubRestConnection();
        restConnection.logger = logger;
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        return hubServicesFactory;
    }

    public File getFile(final String classpathResource) {
        try {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
            final File file = new File(url.toURI().getPath());
            return file;
        } catch (final Exception e) {
            fail("Could not get file: " + e.getMessage());
            return null;
        }
    }

}
