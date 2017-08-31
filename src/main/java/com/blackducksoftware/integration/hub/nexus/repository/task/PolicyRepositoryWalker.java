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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.Map;

import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class PolicyRepositoryWalker extends AbstractWalkerProcessor {
    private final EventBus eventBus;
    private final ItemAttributesHelper itemAttributesHelper;
    private final Map<String, String> taskParameters;
    private final HubServiceHelper hubServiceHelper;

    public PolicyRepositoryWalker(final EventBus eventBus, final ItemAttributesHelper itemAttributesHelper, final Map<String, String> taskParameters, final HubServiceHelper hubServiceHelper) {
        this.itemAttributesHelper = itemAttributesHelper;
        this.eventBus = eventBus;
        this.taskParameters = taskParameters;
        this.hubServiceHelper = hubServiceHelper;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {

    }
}
