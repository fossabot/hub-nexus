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
package com.blackducksoftware.integration.hub.nexus.event;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.test.TestEventBus;
import com.blackducksoftware.integration.hub.nexus.test.TestingPropertyKey;

public class HubScanEventHandlerTestIT extends AbstractMavenRepoContentTests {
    private RestConnectionTestHelper restConnection;
    private ApplicationConfiguration appConfiguration;
    private TestEventBus eventBus;
    private DefaultAttributesHandler attributesHandler;
    private ScanEventManager eventManager;
    private Repository repository;
    private Map<String, String> taskParameters;
    private ResourceStoreRequest resourceStoreRequest;
    private StorageItem item;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        eventBus = new TestEventBus();
        appConfiguration = this.nexusConfiguration();
        attributesHandler = lookup(DefaultAttributesHandler.class);
        eventManager = new ScanEventManager(eventBus);
        restConnection = new RestConnectionTestHelper();
    }

    @Override
    protected boolean runWithSecurityDisabled() {
        return true;
    }

    @Before
    public void initTest() throws NoSuchRepositoryException, Exception {
        final File zipFile = getTestFile("src/test/resources/repo1/aa-1.2.3.zip");
        final File propFile = getTestFile("src/test/resources/repo1/packaging2extension-mapping.properties");
        resourceStoreRequest = new ResourceStoreRequest("/integration/test/1.0-SNAPSHOT/" + zipFile.getName());
        repository = lookup(RepositoryRegistry.class).getRepositoryWithFacet("snapshots", MavenHostedRepository.class);
        lookup(ArtifactPackagingMapper.class).setPropertiesFile(propFile);
        repository.storeItem(resourceStoreRequest, new FileInputStream(zipFile), null);
        item = repository.retrieveItem(resourceStoreRequest);
        taskParameters = generateParams();
        taskParameters.put(ScanEventManager.PARAMETER_KEY_TASK_NAME, "IntegationTestTask");
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
    public void testProcessedEvent() {
        final HubScanEvent event = new HubScanEvent(repository, item, taskParameters, resourceStoreRequest, null);
        event.setProcessed(true);
        final HubScanEventHandler eventHandler = new HubScanEventHandler(appConfiguration, eventBus, attributesHandler, eventManager);
        eventHandler.handle(event);
        Assert.assertFalse(eventBus.hasEvents());
        Assert.assertTrue(event.isProcessed());
    }

    @Test
    public void testHandleEvent() {
        final HubScanEvent event = new HubScanEvent(null, null, null, null, null);
        final HubScanEventHandler eventHandler = new HubScanEventHandler(appConfiguration, eventBus, attributesHandler, eventManager);
        eventHandler.handle(event);
        Assert.assertTrue(eventBus.hasEvents());
        Assert.assertTrue(event.isProcessed());
    }

    public void testProjectVersionViewNull() {

    }
}
