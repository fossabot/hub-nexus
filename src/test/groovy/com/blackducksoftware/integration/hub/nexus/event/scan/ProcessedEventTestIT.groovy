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
package com.blackducksoftware.integration.hub.nexus.event.scan

import org.junit.Assert
import org.junit.Test

import com.blackducksoftware.integration.hub.nexus.event.AbstractHandlerTest
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent
import com.blackducksoftware.integration.hub.nexus.event.HubScanEventHandler

public class ProcessedEventTestIT extends AbstractHandlerTest {

    @Override
    public String getZipFilePath() {
        return "src/test/resources/repo1/aa-1.2.3.zip"
    }

    @Test
    public void testProcessedEvent() {
        final HubScanEvent event = new HubScanEvent(getRepository(), getItem(), getTaskParameters(), getResourceStoreRequest(), getProjectRequest())
        event.setProcessed(true)
        final HubScanEventHandler eventHandler = new HubScanEventHandler(getAppConfiguration(), getEventBus(), getAttributesHandler(), getEventManager())
        eventHandler.handle(event)
        Assert.assertFalse(getEventBus().hasEvents())
    }
}
