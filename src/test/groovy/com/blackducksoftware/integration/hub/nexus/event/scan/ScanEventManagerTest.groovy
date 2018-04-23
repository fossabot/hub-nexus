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
package com.blackducksoftware.integration.hub.nexus.event.scan

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.sonatype.nexus.events.Event
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.repository.Repository

import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData
import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager
import com.blackducksoftware.integration.hub.nexus.test.TestEventBus

import groovy.transform.TypeChecked

@TypeChecked
public class ScanEventManagerTest {

    public static String TEST_TASK_NAME = "IntegrationTestTask"

    private TestEventBus eventBus
    private TaskEventManager eventManager
    private StorageItem item

    @Before
    public void init() {
        eventBus = new TestEventBus()
        eventManager = new TaskEventManager(eventBus)
        item = Mockito.mock(StorageItem.class)
        final Repository repository = Mockito.mock(Repository.class)
        final RepositoryItemUid repositoryUid = Mockito.mock(RepositoryItemUid.class)
        Mockito.when(repositoryUid.getRepository()).thenReturn(repository)
        Mockito.when(item.getRepositoryItemUid()).thenReturn(repositoryUid)
    }

    @After
    public void cleanUp() {
        eventBus.removeAllEvents()
    }

    @Test
    public void testProcessItemNullParameterMap() throws Exception {
        final ScanItemMetaData data = new ScanItemMetaData(item, null, null, null)
        eventManager.addNewEvent(processItem(data))
        Assert.assertFalse(eventBus.hasEvents())
    }

    @Test
    public void testProcessItem() throws Exception {
        final Map<String, String> taskParameters = new HashMap<>()
        taskParameters.put(TaskEventManager.PARAMETER_KEY_TASK_NAME, TEST_TASK_NAME)
        final ScanItemMetaData data = new ScanItemMetaData(item, null, taskParameters, null)
        eventManager.addNewEvent(processItem(data))
        Assert.assertTrue(eventBus.hasEvents())
    }

    @Test
    public void testPendingEventCount() throws Exception {
        Assert.assertEquals(0, eventManager.pendingEventCount(TEST_TASK_NAME))
        final Map<String, String> taskParameters = new HashMap<>()
        taskParameters.put(TaskEventManager.PARAMETER_KEY_TASK_NAME, TEST_TASK_NAME)
        final ScanItemMetaData data = new ScanItemMetaData(item, null, taskParameters, null)
        eventManager.addNewEvent(processItem(data))
        Assert.assertTrue(eventBus.hasEvents())
        Assert.assertEquals(eventBus.getEventCount(), eventManager.pendingEventCount(TEST_TASK_NAME))
    }

    @Test
    public void testMarkEventProcessed() throws Exception {
        Assert.assertEquals(0, eventManager.pendingEventCount(TEST_TASK_NAME))
        final Map<String, String> taskParameters = new HashMap<>()
        taskParameters.put(TaskEventManager.PARAMETER_KEY_TASK_NAME, TEST_TASK_NAME)
        final ScanItemMetaData data = new ScanItemMetaData(item, null, taskParameters, null)
        eventManager.addNewEvent(processItem(data))
        Assert.assertTrue(eventBus.hasEvents())
        Assert.assertEquals(eventBus.getEventCount(), eventManager.pendingEventCount(TEST_TASK_NAME))

        final Collection<Event<?>> eventCollection = eventBus.getEvents()
        for (final Event<?> event : eventCollection) {
            if (event instanceof HubScanEvent) {
                final HubScanEvent scanEvent = (HubScanEvent) event
                eventManager.markScanEventProcessed(scanEvent)
                Assert.assertTrue(scanEvent.isProcessed())
            }
        }
        Assert.assertEquals(0, eventManager.pendingEventCount(TEST_TASK_NAME))
    }

    private HubScanEvent processItem(final ScanItemMetaData data) {
        final HubScanEvent event = new HubScanEvent(data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest(), data.getProjectRequest());
        return event;
    }
}
