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
package com.blackducksoftware.integration.hub.nexus.event.handler;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubEvent;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public abstract class HubEventHandler<E extends HubEvent> implements Runnable {
    private final ItemAttributesHelper attributeHelper;
    private E event;
    private final HubServiceHelper hubServiceHelper;

    public HubEventHandler(final ItemAttributesHelper itemAttributesHelper, final E event, final HubServiceHelper hubServiceHelper) {
        this.attributeHelper = itemAttributesHelper;
        this.event = event;
        this.hubServiceHelper = hubServiceHelper;
    }

    public ItemAttributesHelper getAttributeHelper() {
        return attributeHelper;
    }

    public E getEvent() {
        return event;
    }

    public void setEvent(final E event) {
        this.event = event;
    }

    public HubServiceHelper getHubServiceHelper() {
        return hubServiceHelper;
    }
}
