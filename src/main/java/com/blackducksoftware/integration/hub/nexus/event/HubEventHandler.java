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

import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class HubEventHandler extends ComponentSupport implements EventSubscriber, Asynchronous {
    private final ItemAttributesHelper attributeHelper;

    public HubEventHandler(final AttributesHandler attributesHandler) {
        this.attributeHelper = new ItemAttributesHelper(attributesHandler);
    }

    public ItemAttributesHelper getAttributeHelper() {
        return attributeHelper;
    }

    public HubServiceHelper createServiceHelper(final HubEventLogger logger, final Map<String, String> taskParameters) {
        return new HubServiceHelper(logger, taskParameters);
    }
}
