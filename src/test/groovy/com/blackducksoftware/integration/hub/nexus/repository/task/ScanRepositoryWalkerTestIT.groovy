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
package com.blackducksoftware.integration.hub.nexus.repository.task

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.sonatype.nexus.proxy.attributes.Attributes
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.walker.WalkerContext

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.event.ScanEventManager
import com.blackducksoftware.integration.hub.nexus.event.ScanItemMetaData
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.nexus.test.TestEventLogger
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(ScanRepositoryWalker.class)
public class ScanRepositoryWalkerTestIT {

    @Mock
    ItemAttributesHelper itemAttributesHelper

    StorageItem item

    @Mock
    ScanEventManager scanEventManager

    WalkerContext walkerContext

    RepositoryItemUid repositoryItemUid

    Attributes attributes

    @Mock
    HubServiceHelper hubServiceHelper

    @Mock
    ScanItemMetaData scanItemMetaData

    @Test
    public void processItemsTest() throws Exception {
        final Map<String, String> taskParams = new HashMap<>()
        taskParams.put(TaskField.DISTRIBUTION.getParameterKey(), "EXTERNAL")
        taskParams.put(TaskField.PHASE.getParameterKey(), "DEVELOPMENT")

        repositoryItemUid = [ getBooleanAttributeValue: { attr -> false }] as RepositoryItemUid

        attributes = [ getModified: { -> 10l }] as Attributes

        item = [ getRepositoryItemUid: { -> repositoryItemUid },
            getRemoteUrl: { -> "" },
            getPath: { -> "test" },
            getRepositoryItemAttributes: { -> attributes },
            getParentPath: { -> "/test/1.1.1" } ] as StorageItem

        Mockito.when(itemAttributesHelper.getScanTime(item)).thenReturn(101L)
        Mockito.when(itemAttributesHelper.getScanResult(item)).thenReturn(ItemAttributesHelper.SCAN_STATUS_SUCCESS)

        final RestConnectionTestHelper restConnection = new RestConnectionTestHelper()
        final HubServiceHelper hubServiceHelper = new HubServiceHelper(new TestEventLogger(), taskParams)
        hubServiceHelper.setHubServicesFactory(restConnection.createHubServicesFactory())

        walkerContext = [ getResourceStoreRequest: { -> null } ] as WalkerContext

        final ScanRepositoryWalker walker = new ScanRepositoryWalker("test", itemAttributesHelper, taskParams, scanEventManager, hubServiceHelper)
        walker.processItem(walkerContext, item)

        doNothing().when(scanEventManager).processItem(scanItemMetaData)
        verify(scanEventManager).processItem(any(ScanItemMetaData.class))
    }
}
