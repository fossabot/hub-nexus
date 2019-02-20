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

import java.util.concurrent.ExecutorService

import org.apache.commons.collections.map.HashedMap
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.slf4j.LoggerFactory
import org.sonatype.nexus.proxy.attributes.Attributes
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.walker.WalkerContext

import com.blackducksoftware.integration.hub.api.project.ProjectRequestService
import com.blackducksoftware.integration.hub.exception.DoesNotExistException
import com.blackducksoftware.integration.hub.model.view.ProjectView
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.ScanRepositoryWalker
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.nexus.test.TestEventLogger
import com.blackducksoftware.integration.hub.nexus.test.TestExecutorService
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.log.Slf4jIntLogger

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(ScanRepositoryWalker.class)
public class ScanRepositoryWalkerTestIT {

    private final static String PARENT_PATH="/test/0.0.1-SNAPSHOT"
    private final static String PROJECT_NAME="test"

    @Mock
    private ItemAttributesHelper itemAttributesHelper
    private StorageItem item
    private WalkerContext walkerContext
    private RepositoryItemUid repositoryItemUid
    private Attributes attributes
    private Map<String,String> taskParameters
    private RestConnectionTestHelper restConnection
    private ParallelEventProcessor parallelEventProcessor
    private TestExecutorService testExecutorService

    @Mock
    HubServiceHelper hubServiceHelper

    @Mock
    ScanItemMetaData scanItemMetaData

    @Before
    public void initTest() {
        restConnection = new RestConnectionTestHelper()
        taskParameters = new HashedMap<>()
        taskParameters.put(TaskField.DISTRIBUTION.getParameterKey(), "EXTERNAL")
        taskParameters.put(TaskField.PHASE.getParameterKey(), "DEVELOPMENT")

        parallelEventProcessor = new ParallelEventProcessor() {
                    @Override
                    public ExecutorService createExecutorService(int availableProcessors) {
                        TestExecutorService testExecutorService = new TestExecutorService()
                        return testExecutorService;
                    }
                }
        testExecutorService = parallelEventProcessor.initializeExecutorService() as TestExecutorService
        repositoryItemUid = [ getBooleanAttributeValue: { attr -> false }, getRepository: { -> null } ] as RepositoryItemUid
        walkerContext = [ getResourceStoreRequest: { -> null } ] as WalkerContext
    }

    @After
    public void cleanUpAfterTest() throws Exception {
        try {
            final Slf4jIntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()))
            final HubServicesFactory hubServicesFactory = restConnection.createHubServicesFactory(intLogger)
            final ProjectRequestService projectRequestService = hubServicesFactory.createProjectRequestService()
            final ProjectView projectView = projectRequestService.getProjectByName(PROJECT_NAME)
            projectRequestService.deleteHubProject(projectView)
        } catch (final DoesNotExistException ex) {
            // ignore if the project doesn't exist then do not fail the test this is just cleanup.
        }
    }

    @Test
    public void processSuccessfullyScanned() throws Exception {
        attributes = [ getModified: { -> 10l }] as Attributes
        item = [ getRepositoryItemUid: { -> repositoryItemUid },
            getRemoteUrl: { -> "" },
            getPath: { -> PROJECT_NAME },
            getRepositoryItemAttributes: { -> attributes },
            getParentPath: { -> PARENT_PATH },
            getName: { -> "itemName" }] as StorageItem

        Mockito.when(itemAttributesHelper.getScanTime(item)).thenReturn(101L)
        Mockito.when(itemAttributesHelper.getScanResult(item)).thenReturn(ItemAttributesHelper.SCAN_STATUS_SUCCESS)


        final HubServiceHelper hubServiceHelper = new HubServiceHelper(new TestEventLogger(), taskParameters)
        hubServiceHelper.setHubServicesFactory(restConnection.createHubServicesFactory())

        final ScanRepositoryWalker walker = new ScanRepositoryWalker(parallelEventProcessor, taskParameters, hubServiceHelper, itemAttributesHelper)
        walker.processItem(walkerContext, item)
        Assert.assertTrue(testExecutorService.hasItems())
    }
}
