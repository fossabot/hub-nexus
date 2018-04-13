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
package com.blackducksoftware.integration.hub.nexus.repository.task

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import org.apache.commons.collections.map.HashedMap
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.sonatype.nexus.events.Event
import org.sonatype.nexus.proxy.attributes.Attributes
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.walker.WalkerContext

import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent
import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.PolicyRepositoryWalker
import com.blackducksoftware.integration.hub.nexus.test.TestEventBus
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper
import com.blackducksoftware.integration.hub.nexus.util.ScanAttributesHelper

@RunWith(MockitoJUnitRunner.class)
public class PolicyWalkerTest {

    private final static String PARENT_PATH="/test/0.0.1-SNAPSHOT"
    private final static String PROJECT_NAME="test"

    @Mock
    private ItemAttributesHelper itemAttributesHelper
    @Mock
    private HubServiceHelper hubServiceHelper
    private StorageItem item
    private WalkerContext walkerContext
    private RepositoryItemUid repositoryItemUid
    private Attributes attributes
    private TestEventBus eventBus
    private TaskEventManager taskEventManager
    private Map<String,String> taskParameters

    @Before
    public void initTest() {
        taskParameters = new HashedMap<>()
        taskParameters.put(TaskField.DISTRIBUTION.getParameterKey(), "EXTERNAL")
        taskParameters.put(TaskField.PHASE.getParameterKey(), "DEVELOPMENT")
        taskParameters.put(TaskEventManager.PARAMETER_KEY_TASK_NAME, PolicyCheckTaskDescriptor.TASK_NAME);

        eventBus = new TestEventBus();
        taskEventManager = new TaskEventManager(eventBus);
        repositoryItemUid = [ getBooleanAttributeValue: { attr -> false }, getRepository: { -> null } ] as RepositoryItemUid

        item = [ getRepositoryItemUid: { -> repositoryItemUid },
            getTaskParameters: { -> taskParameters },
            getRemoteUrl: { -> "" },
            getPath: { -> PROJECT_NAME },
            getRepositoryItemAttributes: { -> attributes },
            getParentPath: { -> PARENT_PATH },
            getName: { -> "itemName" }] as StorageItem
        walkerContext = [ getResourceStoreRequest: { -> null } ] as WalkerContext
        ProjectVersionRequestService projectVersionRequestService = Mockito.mock(ProjectVersionRequestService.class)
        hubServiceHelper = Mockito.mock(HubServiceHelper.class)
        Mockito.when(hubServiceHelper.getProjectVersionRequestService()).thenReturn(projectVersionRequestService)
        Mockito.when(projectVersionRequestService.getItem(Mockito.anyString(), Mockito.any())).thenReturn(Mockito.mock(ProjectVersionView.class))
    }

    @Test
    public void testScanSuccess() {
        final PolicyRepositoryWalker walker = new PolicyRepositoryWalker(itemAttributesHelper, new ScanAttributesHelper(taskParameters), hubServiceHelper, taskEventManager);
        walker.processItem(walkerContext, item)
        assertEquals(1, eventBus.getEventCount())
        Collection<Event<?>> eventCollection = eventBus.getEvents()
        for(Event<?> event : eventCollection ) {
            assertTrue(event instanceof HubPolicyCheckEvent)
            HubPolicyCheckEvent policyEvent = (HubPolicyCheckEvent) event
            assertEquals(item, policyEvent.getItem())
        }
    }
}
