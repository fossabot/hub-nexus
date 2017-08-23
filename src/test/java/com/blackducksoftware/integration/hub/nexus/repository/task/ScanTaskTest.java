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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.ScanEventManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScanTask.class)
public class ScanTaskTest {

    @Mock
    Walker walker;

    @Mock
    DefaultAttributesHandler defaultAttributesHandler;

    @Mock
    ScanEventManager scanEventManager;

    @Mock
    HubServiceHelper hubServiceHelper;

    @Test
    public void getActionTest() {
        final ScanTask scanTask = new ScanTask(null, null, null);
        Assert.assertEquals("BLACKDUCK_HUB_SCAN", scanTask.getAction());
    }

    @Test
    public void getMessageTest() {
        final ScanTask scanTask = new ScanTask(null, null, null);
        Assert.assertEquals("Searching to scan artifacts in the repository", scanTask.getMessage());
    }

    @Test
    public void doRunTest() throws Exception {
        final ScanTask scanTask = new ScanTask(walker, defaultAttributesHandler, scanEventManager);
        final WalkerContext walkerContext = mock(WalkerContext.class);

        when(scanEventManager.pendingEventCount("Hub Repository Scan")).thenReturn(0);

        PowerMockito.whenNew(HubServiceHelper.class).withAnyArguments().thenReturn(hubServiceHelper);
        when(hubServiceHelper.createCLIInstallDirectoryName()).thenReturn("folder");
        doNothing().when(hubServiceHelper).installCLI(mock(File.class));

        Assert.assertNull(scanTask.doRun());
    }

}
