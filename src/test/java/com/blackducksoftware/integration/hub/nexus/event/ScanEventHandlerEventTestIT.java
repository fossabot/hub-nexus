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
package com.blackducksoftware.integration.hub.nexus.event;

import org.junit.Assert;
import org.junit.Test;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;

public class ScanEventHandlerEventTestIT extends AbstractScanHandlerTest {

    @Test
    public void testHandleEvent() throws Exception {
        getTaskParameters().put(TaskField.WORKING_DIRECTORY.getParameterKey(), getWorkHomeDir().getCanonicalPath());
        getTaskParameters().put(TaskField.HUB_SCAN_MEMORY.getParameterKey(), "4096");
        getTaskParameters().put(TaskField.HUB_TIMEOUT.getParameterKey(), "300");
        final HubScanEvent event = new HubScanEvent(getRepository(), getItem(), getTaskParameters(), getResourceStoreRequest(), null);
        final HubScanEventHandler eventHandler = new HubScanEventHandler(getAppConfiguration(), getEventBus(), getAttributesHandler(), getEventManager());
        eventHandler.handle(event);
        Assert.assertTrue(getEventBus().hasEvents());
        Assert.assertTrue(event.isProcessed());
    }
}
