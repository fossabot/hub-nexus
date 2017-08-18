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
package com.blackducksoftware.integration.hub.nexus.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.blackducksoftware.integration.hub.nexus.event.HubEvent;
import com.blackducksoftware.integration.hub.nexus.helpers.TestEventLogger;

public class HubEventLoggerTest {
    @Mock
    private final HubEvent event;

    private final TestEventLogger logger = new TestEventLogger();
    private final HubEventLogger hubLogger;

    private static final String UUID_STRING = "7dc53df5-703e-49b3-8670-b1c468f47f1f";
    private static final String EXPECTED = "Event 7dc53df5-703e-49b3-8670-b1c468f47f1f - expected";

    public HubEventLoggerTest() {
        event = mock(HubEvent.class);
        when(event.getEventId()).thenReturn(UUID.fromString(UUID_STRING));
        hubLogger = new HubEventLogger(event, logger);
    }

    @Test
    public void logDebugTest() {
        hubLogger.debug("expected");
        Assert.assertEquals(EXPECTED, logger.getOutputList().get(logger.getOutputList().size() - 1));
    }

    @Test
    public void logInfoTest() {
        hubLogger.info("expected");
        Assert.assertEquals(EXPECTED, logger.getOutputList().get(logger.getOutputList().size() - 1));
    }

    @Test
    public void logErrorTest() {
        hubLogger.error("expected");
        Assert.assertEquals(EXPECTED, logger.getOutputList().get(logger.getOutputList().size() - 1));
    }

    @Test
    public void logWarnTest() {
        hubLogger.warn("expected");
        Assert.assertEquals(EXPECTED, logger.getOutputList().get(logger.getOutputList().size() - 1));
    }

    @Test
    public void logTraceTest() {
        hubLogger.trace("expected");
        Assert.assertEquals(EXPECTED, logger.getOutputList().get(logger.getOutputList().size() - 1));
    }

    @Test
    public void alwaysLogTest() {
        hubLogger.alwaysLog("expected");
        Assert.assertEquals(EXPECTED, logger.getOutputList().get(logger.getOutputList().size() - 1));
    }
}
