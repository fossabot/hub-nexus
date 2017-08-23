/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
