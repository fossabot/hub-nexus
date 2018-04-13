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
package com.blackducksoftware.integration.hub.nexus.event.policy

import org.junit.Assert
import org.junit.Test
import org.sonatype.nexus.events.Event

import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent
import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager
import com.blackducksoftware.integration.hub.nexus.event.handler.HubPolicyCheckEventHandler
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper

public class InViolationPolicyCheckEventIT extends AbstractPolicyCheckTest {

    @Override
    public String getZipFilePath() {
        return "src/test/resources/repo1/hub-teamcity-3.1.0.zip"
    }

    @Test
    public void testHandleEvent() throws Exception {
        TaskEventManager taskEventManager = new TaskEventManager(eventBus)
        final HubPolicyCheckEventHandler policyEventHandler = new HubPolicyCheckEventHandler(getAttributesHandler(), taskEventManager)

        for (final Event<?> event : getEventBus().getEvents()) {
            if (event instanceof HubPolicyCheckEvent) {
                final HubPolicyCheckEvent scanEvent = (HubPolicyCheckEvent) event
                policyEventHandler.handle(scanEvent)
                Assert.assertTrue(getEventBus().hasEvents())
                final ItemAttributesHelper itemAttributesHelper = new ItemAttributesHelper(getAttributesHandler())
                final String overallStatus = itemAttributesHelper.getOverallPolicyStatus(getItem())
                final String policyMessage = itemAttributesHelper.getPolicyStatus(getItem())
                Assert.assertNotNull(overallStatus)
                Assert.assertNotNull(policyMessage)
                Assert.assertEquals("In Violation", overallStatus)
                Assert.assertEquals("The Hub found: 1 components in violation, 0 components in violation, but overridden, and 20 components not in violation.", policyMessage)
            }
        }
    }
}
