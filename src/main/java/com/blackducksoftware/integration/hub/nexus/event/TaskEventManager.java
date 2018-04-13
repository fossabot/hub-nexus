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
package com.blackducksoftware.integration.hub.nexus.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.sisu.goodies.eventbus.EventBus;

@Named
@Singleton
public class TaskEventManager {
    public static final String PARAMETER_KEY_TASK_NAME = ".name";
    private final Logger logger = LoggerFactory.getLogger(TaskEventManager.class);
    private final EventBus eventBus;
    private final Map<String, Map<String, HubEvent>> taskScanEventMap;
    private int maxEvents;

    @Inject
    public TaskEventManager(final EventBus eventBus) {
        this.eventBus = eventBus;
        this.maxEvents = 5;
        this.taskScanEventMap = new ConcurrentHashMap<>();
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(final int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public int pendingEventCount(final String taskName) {
        if (!taskScanEventMap.containsKey(taskName)) {
            return 0;
        } else {
            final Map<String, HubEvent> eventMap = taskScanEventMap.get(taskName);
            return eventMap.size();
        }
    }

    public void markScanEventProcessed(final HubEvent event) {
        if (event.getTaskParameters() != null) {
            final String taskName = event.getTaskParameters().get(PARAMETER_KEY_TASK_NAME);
            final String eventId = event.getEventId().toString();
            markEventProcessed(taskName, eventId);
        }
    }

    private void markEventProcessed(final String taskName, final String eventId) {
        if (taskScanEventMap.containsKey(taskName)) {
            final Map<String, HubEvent> eventMap = taskScanEventMap.get(taskName);
            if (eventMap.containsKey(eventId)) {
                final HubEvent event = eventMap.remove(eventId);
                event.setProcessed(true);
            }
        }
    }

    public boolean processEvent(final HubEvent event) {
        final String taskName = event.getTaskParameters().get(PARAMETER_KEY_TASK_NAME);
        final int pendingEvents = pendingEventCount(taskName);

        if (pendingEvents < maxEvents) {
            logger.info("Attempting to add {} to event bus.", event);
            addNewEvent(event, taskName, pendingEvents);
            return true;
        } else {
            logger.info("Too many items on event bus.");
        }

        return false;
    }

    private void addNewEvent(final HubEvent event, final String taskName, final int pendingCount) {
        if (event.getTaskParameters() != null) {
            Map<String, HubEvent> eventMap;
            if (taskName != null) {
                if (taskScanEventMap.containsKey(taskName)) {
                    eventMap = taskScanEventMap.get(taskName);
                } else {
                    eventMap = new ConcurrentHashMap<>(1000);
                    taskScanEventMap.put(taskName, eventMap);
                }
                logger.info("Posting new item to event bus, now have {} pending items.", pendingCount + 1);
                eventMap.put(event.getEventId().toString(), event);
                eventBus.post(event);
            }
        }
    }
}
