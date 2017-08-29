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

import org.junit.Assert;
import org.junit.Test;

public class ScanTaskTestIT {
    // private final Walker walker;
    // private final DefaultAttributesHandler defaultAttributesHandler;
    // private final ScanEventManager scanEventManager;
    //
    // @Inject
    // public ScanTaskTestIT(final Walker walker, final DefaultAttributesHandler defaultAttributesHandler, final ScanEventManager scanEventManager) {
    // this.walker = walker;
    // this.defaultAttributesHandler = defaultAttributesHandler;
    // this.scanEventManager = scanEventManager;
    // }

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
        final ScanTask scanTask = new ScanTask(null, null, null);
        final Object nothing = scanTask.doRun();
        Assert.assertNull(nothing);
    }

}
