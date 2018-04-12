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
import java.util.UUID;

import org.sonatype.nexus.events.AbstractEvent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

public class HubEvent extends AbstractEvent<Repository> {
    private final StorageItem item;
    private final Map<String, String> taskParameters;
    private final ResourceStoreRequest request;
    private final UUID eventId;
    private boolean processed;

    public HubEvent(final Repository repository, final StorageItem item, final Map<String, String> taskParameters, final ResourceStoreRequest request) {
        super(repository);
        this.item = item;
        this.taskParameters = taskParameters;
        this.request = request;
        this.eventId = UUID.randomUUID();
        processed = false;
    }

    public UUID getEventId() {
        return eventId;
    }

    public ResourceStoreRequest getRequest() {
        return request;
    }

    public Repository getRepository() {
        return getEventSender();
    }

    public StorageItem getItem() {
        return item;
    }

    public Map<String, String> getTaskParameters() {
        return taskParameters;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(final boolean processed) {
        this.processed = processed;
    }

}
