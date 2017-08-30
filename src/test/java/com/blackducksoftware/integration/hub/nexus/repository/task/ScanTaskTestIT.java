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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.walker.Walker;

import com.blackducksoftware.integration.hub.nexus.event.ScanEventManager;
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.test.TestingPropertyKey;

public class ScanTaskTestIT extends AbstractMavenRepoContentTests {
    private Walker walker;
    private DefaultAttributesHandler defaultAttributesHandler;
    private ScanEventManager scanEventManager;
    private RestConnectionTestHelper restConnection;
    private NexusConfiguration appConfiguration;
    private Map<String, String> taskParameters;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        appConfiguration = this.nexusConfiguration();
        defaultAttributesHandler = lookup(DefaultAttributesHandler.class);
        restConnection = new RestConnectionTestHelper();
    }

    @Before
    public void init() throws Exception {
        walker = lookup(Walker.class);
        defaultAttributesHandler = lookup(DefaultAttributesHandler.class);
        scanEventManager = lookup(ScanEventManager.class);
        taskParameters = generateParams();
        taskParameters.put(ScanEventManager.PARAMETER_KEY_TASK_NAME, "IntegationTestTask");
    }

    @Test
    public void doRunTest() throws Exception {

        final ScanTask scanTask = new ScanTask(walker, defaultAttributesHandler, scanEventManager);
        final ScanTask spyScanTask = Mockito.spy(scanTask);
        final Walker spyWalker = Mockito.spy(walker);

        Mockito.when(spyScanTask.getParameters()).thenReturn(taskParameters);

        final Object nothing = spyScanTask.doRun();
        Assert.assertNull(nothing);
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

}
