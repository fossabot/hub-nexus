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

import com.blackducksoftware.integration.hub.nexus.event.HubEvent;
import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager;
import com.blackducksoftware.integration.hub.nexus.exception.WalkerExitException;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.ScanAttributesHelper;

public abstract class RepositoryWalkerProcessor extends AbstractWalkerProcessor {
    private final Logger logger = Loggers.getLogger(getClass());
    protected final ScanAttributesHelper scanAttributesHelper;
    protected final TaskEventManager taskEventManager;
    protected final int maxParallelEvents;

    public RepositoryWalkerProcessor(final ScanAttributesHelper scanAttributesHelper, final TaskEventManager taskEventManager, final int maxParallelEvents) {
        this.scanAttributesHelper = scanAttributesHelper;
        this.taskEventManager = taskEventManager;
        this.maxParallelEvents = maxParallelEvents;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        try {
            logger.info("Item pending scan {}", item);
            final HubEvent hubEvent = createEvent(context, item);

            final long startTime = System.currentTimeMillis();
            final long timeoutInSeconds = scanAttributesHelper.getIntegerAttribute(TaskField.HUB_TIMEOUT);
            final long timeoutInMillis = TimeUnit.SECONDS.toMillis(timeoutInSeconds);
            long activeTime = 0;
            while (!taskEventManager.processItem(hubEvent, maxParallelEvents) && (activeTime < timeoutInMillis)) {
                final long currentTime = System.currentTimeMillis();
                activeTime = currentTime - startTime;
            }

            if (activeTime >= timeoutInMillis) {
                context.stop(new WalkerExitException("Repository walker timed out"));
            }
        } catch (final Exception ex) {
            logger.error("Error occurred in walker processor for repository: ", ex);
        }
    }

    public abstract HubEvent createEvent(WalkerContext context, StorageItem item) throws Exception;

}
