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
package com.blackducksoftware.integration.hub.nexus.test

import org.sonatype.nexus.events.Event
import org.sonatype.sisu.goodies.eventbus.EventBus

import com.blackducksoftware.integration.hub.nexus.event.HubEvent
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent

public class TestEventBus implements EventBus {

    Map<String, Event<?>> eventMap = new HashMap<>()

    @Override
    public EventBus register(final Object handler) {
        return null
    }

    @Override
    public EventBus unregister(final Object handler) {
        return null
    }

    @Override
    public EventBus post(final Object event) {
        if (event instanceof HubEvent) {
            final HubEvent hubEvent = (HubEvent) event
            eventMap.put(hubEvent.getEventId().toString(), hubEvent)
        }
        return this
    }

    public boolean containsEvent(final HubScanEvent event) {
        return eventMap.containsKey(event.getEventId().toString())
    }

    public boolean hasEvents() {
        return !eventMap.isEmpty()
    }

    public int getEventCount() {
        return eventMap.size()
    }

    public void removeAllEvents() {
        eventMap.clear()
    }

    public Collection<Event<?>> getEvents() {
        return eventMap.values()
    }
}
