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
package com.blackducksoftware.integration.hub.nexus.repository.task

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.event.handler.HubPolicyCheckEventHandler
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.PolicyRepositoryWalker
import com.blackducksoftware.integration.hub.nexus.test.TestExecutorService
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView
import com.synopsys.integration.blackduck.service.BlackDuckService
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import org.apache.commons.collections.map.HashedMap
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.sonatype.nexus.proxy.attributes.Attributes
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.walker.WalkerContext

import java.util.concurrent.ExecutorService

@RunWith(MockitoJUnitRunner.class)
public class PolicyWalkerTest {

    private final static String PARENT_PATH = "/test/0.0.1-SNAPSHOT"
    private final static String PROJECT_NAME = "test"

    @Mock
    private ItemAttributesHelper itemAttributesHelper

    @Mock
    private HubServiceHelper hubServiceHelper

    private StorageItem item
    private WalkerContext walkerContext
    private RepositoryItemUid repositoryItemUid
    private Attributes attributes
    private Map<String, String> taskParameters
    private ParallelEventProcessor parallelEventProcessor
    private TestExecutorService testExecutorService

    @Before
    public void initTest() {
        taskParameters = new HashedMap<>()
        taskParameters.put(TaskField.DISTRIBUTION.getParameterKey(), "EXTERNAL")
        taskParameters.put(TaskField.PHASE.getParameterKey(), "DEVELOPMENT")

        repositoryItemUid = [getBooleanAttributeValue: { attr -> false }, getRepository: { -> null }] as RepositoryItemUid

        item = [getRepositoryItemUid       : { -> repositoryItemUid },
                getRemoteUrl               : { -> "" },
                getPath                    : { -> PROJECT_NAME },
                getRepositoryItemAttributes: { -> attributes },
                getParentPath              : { -> PARENT_PATH },
                getName                    : { -> "itemName" }] as StorageItem
        walkerContext = [getResourceStoreRequest: { -> null }] as WalkerContext
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class)
        BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class)
        hubServiceHelper = Mockito.mock(HubServiceHelper.class)
        Mockito.when(hubServiceHelper.createBlackDuckServicesFactory()).thenReturn(blackDuckServicesFactory)
        Mockito.when(blackDuckServicesFactory.createBlackDuckService()).thenReturn(blackDuckService)
        ProjectVersionView projectVersionView = Mockito.mock(ProjectVersionView.class);
        Mockito.when(blackDuckService.getResponse(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(projectVersionView)

        parallelEventProcessor = new ParallelEventProcessor() {
            @Override
            public ExecutorService createExecutorService(int availableProcessors) {
                TestExecutorService testExecutorService = new TestExecutorService()
                return testExecutorService;
            }
        }
        testExecutorService = parallelEventProcessor.initializeExecutorService() as TestExecutorService
    }

    @Test
    public void testScanSuccess() {
        Mockito.when(itemAttributesHelper.getScanResult(item)).thenReturn(ItemAttributesHelper.SCAN_STATUS_SUCCESS)

        final PolicyRepositoryWalker walker = new PolicyRepositoryWalker(parallelEventProcessor, itemAttributesHelper, taskParameters, hubServiceHelper);
        walker.processItem(walkerContext, item)
        Assert.assertEquals(1, testExecutorService.getSize())
        Assert.assertTrue(testExecutorService.getItem(0) instanceof HubPolicyCheckEventHandler)
    }
}
