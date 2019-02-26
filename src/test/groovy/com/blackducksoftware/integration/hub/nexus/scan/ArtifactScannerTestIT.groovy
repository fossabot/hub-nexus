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
package com.blackducksoftware.integration.hub.nexus.scan

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.event.policy.AbstractPolicyCheckTest
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.nexus.test.TestingPropertyKey
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.PrintStreamIntLogger
import org.junit.Assert
import org.junit.Before
import org.junit.Test

public class ArtifactScannerTestIT extends AbstractPolicyCheckTest {
    private RestConnectionTestHelper restConnection
    private Map<String, String> taskParams
    private IntLogger logger;

    private Map<String, String> generateParams() {
        final Map<String, String> newParams = new HashMap<>()

        newParams.put(TaskField.HUB_URL.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL))
        newParams.put(TaskField.HUB_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_USERNAME))
        newParams.put(TaskField.HUB_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PASSWORD))
        newParams.put(TaskField.HUB_TIMEOUT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT))
        newParams.put(TaskField.HUB_PROXY_HOST.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_HOST_BASIC))
        newParams.put(TaskField.HUB_PROXY_PORT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PORT_BASIC))
        newParams.put(TaskField.HUB_PROXY_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_USER_BASIC))
        newParams.put(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PASSWORD_BASIC))
        newParams.put(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_AUTO_IMPORT_HTTPS_CERT))

        return newParams
    }

    @Override
    public void setUp() throws Exception {
        super.setUp()
        restConnection = new RestConnectionTestHelper()
        logger = new PrintStreamIntLogger();
    }

    @Override
    public String getZipFilePath() {
        return 'src/test/resources/repo1/aa-1.2.3.zip'
    }

    @Before
    public void init() throws Exception {
        taskParams = generateParams()
    }

    @Test
    public void scanTest() {
        ItemAttributesHelper itemAttributesHelper = new ItemAttributesHelper(super.getAttributesHandler())
        File installDirectory = new File(getClass().getClassLoader().getResource('repo1').getFile())
        HubServiceHelper hubServiceHelper = new HubServiceHelper(logger, taskParams)

        ProjectVersionView projectVersionView = getPolicyCheckEvent().projectVersionView
        Assert.assertNotNull(projectVersionView._meta)
        Assert.assertNotNull(projectVersionView.distribution)
        Assert.assertNotNull(projectVersionView.phase)
    }
}
