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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.sisu.goodies.eventbus.EventBus;

public class ScanEventManager {

    private final Logger logger = LoggerFactory.getLogger(ScanEventManager.class);
    private final List<EventData> eventList;
    private final EventBus eventBus;

    public ScanEventManager(final EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventList = new ArrayList<>(1000);
    }

    public void addNewEvent(final StorageItem item, final ResourceStoreRequest request, final Map<String, String> taskParameters) {
        final EventData event = new EventData(item, request, taskParameters);
        eventList.add(event);
    }

    public void processEvents() {
        try {
            if (eventList.isEmpty()) {
                logger.info("No Scan events to process");
            } else {
                final int eventCount = eventList.size();
                logger.info("Begin Processing {} scan events", eventCount);
                final CountDownLatch lock = new CountDownLatch(eventCount);
                for (final EventData data : eventList) {
                    final HubScanEvent event = new HubScanEvent(lock, data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest());
                    logger.debug("Posting event {} onto the event bus", event.getEventId());
                    eventBus.post(event);
                }
                lock.await();
                logger.info("Finished processing {} scan events", eventCount);
            }
        } catch (final InterruptedException e) {
            logger.error("Error processing events", e);
        }
    }

    private class EventData {
        private final StorageItem item;
        private final ResourceStoreRequest request;
        private final Map<String, String> taskParameters;

        public EventData(final StorageItem item, final ResourceStoreRequest request, final Map<String, String> taskParameters) {
            this.item = item;
            this.request = request;
            this.taskParameters = taskParameters;
        }

        public StorageItem getItem() {
            return item;
        }

        public ResourceStoreRequest getRequest() {
            return request;
        }

        public Map<String, String> getTaskParameters() {
            return taskParameters;
        }
    }
}
