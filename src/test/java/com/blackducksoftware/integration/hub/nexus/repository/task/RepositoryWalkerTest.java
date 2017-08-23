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

import org.mockito.Mock;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;

import com.blackducksoftware.integration.hub.nexus.event.ScanEventManager;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class RepositoryWalkerTest {

    @Mock
    ItemAttributesHelper itemAttributesHelper;

    @Mock
    StorageItem item;

    @Mock
    ScanEventManager scanEventManager;

    @Mock
    WalkerContext walkerContext;

    // @Test
    // public void processItemsTest() throws Exception {
    // when(item.getRepositoryItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)).thenReturn(false);
    // when(item.getRemoteUrl()).thenReturn("NotEmpty");
    //
    // when(itemAttributesHelper.getScanTime(item)).thenReturn(10l);
    // when(itemAttributesHelper.getScanResult(item)).thenReturn(ItemAttributesHelper.SCAN_STATUS_SUCCESS);
    //
    // doNothing().when(scanEventManager);
    //
    // when(walkerContext.getResourceStoreRequest()).thenReturn(null);
    //
    // final Map<String, String> taskParams = new HashMap<>();
    // taskParams.put(TaskField.DISTRIBUTION.getParameterKey(), "distribution");
    // taskParams.put(TaskField.PHASE.getParameterKey(), "phase");
    // taskParams.put(TaskField.OLD_ARTIFACT_CUTOFF.getParameterKey(), "2016-01-01T00:00:00.000");
    // taskParams.put(TaskField.RESCAN_FAILURES.getParameterKey(), "false");
    //
    // final RepositoryWalker walker = new RepositoryWalker("", itemAttributesHelper, taskParams, scanEventManager);
    // walker.processItem(walkerContext, item);
    // }
}
