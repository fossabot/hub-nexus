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
package com.blackducksoftware.integration.hub.nexus.repository.task.walker;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent;
import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager;
import com.blackducksoftware.integration.hub.nexus.exception.MaxWalkedItemsException;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.nexus.util.ScanAttributesHelper;

public class PolicyRepositoryWalker extends AbstractWalkerProcessor {
    private static final int MAX_POLICY_CHECKS = 15;
    private final Logger logger = Loggers.getLogger(getClass());
    private final ItemAttributesHelper itemAttributesHelper;
    private final ScanAttributesHelper scanAttributesHelper;
    private final HubServiceHelper hubServiceHelper;
    private final TaskEventManager taskEventManager;

    public PolicyRepositoryWalker(final ItemAttributesHelper itemAttributesHelper, final ScanAttributesHelper scanAttributesHelper, final HubServiceHelper hubServiceHelper, final TaskEventManager taskEventManager) {
        this.itemAttributesHelper = itemAttributesHelper;
        this.scanAttributesHelper = scanAttributesHelper;
        this.hubServiceHelper = hubServiceHelper;
        this.taskEventManager = taskEventManager;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        try {
            logger.info("Begin Policy check for item {}", item);
            final ProjectVersionView projectVersionView = getProjectVersion(item);
            final HubPolicyCheckEvent event = new HubPolicyCheckEvent(item.getRepositoryItemUid().getRepository(), item, scanAttributesHelper.getScanAttributes(), context.getResourceStoreRequest(), projectVersionView);

            int currentAttempts = 0;
            while (!taskEventManager.processEvent(event) && (currentAttempts < MAX_POLICY_CHECKS)) {
                logger.warn("Attempting to push to event bus again...");
                currentAttempts++;
                TimeUnit.SECONDS.sleep(1);
            }

            if (currentAttempts == MAX_POLICY_CHECKS) {
                logger.warn("Tried processing event too many times, exiting task.");
                context.stop(new MaxWalkedItemsException(MAX_POLICY_CHECKS));
            }
        } catch (final Exception ex) {
            logger.error("Error occurred in walker processor for repository: ", ex);
        }
    }

    private ProjectVersionView getProjectVersion(final StorageItem item) throws IntegrationException {
        final String url = itemAttributesHelper.getApiUrl(item);
        final ProjectVersionView projectVersionView = hubServiceHelper.getProjectVersionRequestService().getItem(url, ProjectVersionView.class);
        return projectVersionView;
    }
}
