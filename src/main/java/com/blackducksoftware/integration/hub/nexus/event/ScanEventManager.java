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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

@Named
@Singleton
public class ScanEventManager extends ComponentSupport {

    public static final String PARAMETER_KEY_TASK_NAME = ".name";
    private final Logger logger = LoggerFactory.getLogger(ScanEventManager.class);
    private final EventBus eventBus;
    private final Map<String, Map<String, HubScanEvent>> taskScanEventMap;

    @Inject
    public ScanEventManager(final EventBus eventBus) {
        this.eventBus = eventBus;
        this.taskScanEventMap = new ConcurrentHashMap<>();
    }

    public int pendingEventCount(final String taskName) {
        if (!taskScanEventMap.containsKey(taskName)) {
            return 0;
        } else {
            final Map<String, HubScanEvent> eventMap = taskScanEventMap.get(taskName);
            return eventMap.size();
        }
    }

    public void markScanEventProcessed(final HubScanEvent event) {
        if (event.getTaskParameters() != null) {
            final String taskName = event.getTaskParameters().get(PARAMETER_KEY_TASK_NAME);
            final String eventId = event.getEventId().toString();
            markEventProcessed(taskName, eventId);
        }
    }

    private void markEventProcessed(final String taskName, final String eventId) {
        if (taskScanEventMap.containsKey(taskName)) {
            final Map<String, HubScanEvent> eventMap = taskScanEventMap.get(taskName);
            if (eventMap.containsKey(eventId)) {
                final HubScanEvent event = eventMap.remove(eventId);
                event.setProcessed(true);
            }
        }
    }

    public void processItem(final ScanItemMetaData data) throws InterruptedException {
        final HubScanEvent event = new HubScanEvent(data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest(), data.getProjectRequest());
        logger.debug("Posting event {} onto the event bus", event.getEventId());
        addNewEvent(event);
    }

    private void addNewEvent(final HubScanEvent event) {
        if (event.getTaskParameters() != null) {
            final String taskName = event.getTaskParameters().get(PARAMETER_KEY_TASK_NAME);
            Map<String, HubScanEvent> eventMap;
            if (taskName != null) {
                if (taskScanEventMap.containsKey(taskName)) {
                    eventMap = taskScanEventMap.get(taskName);
                } else {
                    eventMap = new ConcurrentHashMap<>(1000);
                    taskScanEventMap.put(taskName, eventMap);
                }
                eventMap.put(event.getEventId().toString(), event);
                eventBus.post(event);
            }
        }
    }
}
