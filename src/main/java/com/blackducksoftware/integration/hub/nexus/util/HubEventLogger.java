/*
 * hub-nexus
 *
 * 	Copyright (C) 2019 Black Duck Software, Inc.
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

import org.slf4j.Logger;

import com.blackducksoftware.integration.hub.nexus.event.HubEvent;
import com.synopsys.integration.log.Slf4jIntLogger;

public class HubEventLogger extends Slf4jIntLogger {
    private final HubEvent event;

    public HubEventLogger(final HubEvent event, final Logger logger) {
        super(logger);
        this.event = event;
    }

    private String eventInfoMessage(final String txt) {
        return String.format("Event %s - %s", event.getEventId().toString(), txt);
    }

    @Override
    public void alwaysLog(final String txt) {
        final String message = eventInfoMessage(txt);
        super.alwaysLog(message);
    }

    @Override
    public void info(final String txt) {
        final String message = eventInfoMessage(txt);
        super.info(message);
    }

    @Override
    public void error(final Throwable t) {
        error("", t);
    }

    @Override
    public void error(final String txt, final Throwable t) {
        final String message = eventInfoMessage(txt);
        super.error(message, t);
    }

    @Override
    public void error(final String txt) {
        final String message = eventInfoMessage(txt);
        super.error(message);
    }

    @Override
    public void warn(final String txt) {
        final String message = eventInfoMessage(txt);
        super.warn(message);
    }

    @Override
    public void trace(final String txt) {
        final String message = eventInfoMessage(txt);
        super.trace(message);
    }

    @Override
    public void trace(final String txt, final Throwable t) {
        final String message = eventInfoMessage(txt);
        super.trace(message, t);
    }

    @Override
    public void debug(final String txt) {
        final String message = eventInfoMessage(txt);
        super.debug(message);
    }

    @Override
    public void debug(final String txt, final Throwable t) {
        final String message = eventInfoMessage(txt);
        super.debug(message, t);
    }
}
