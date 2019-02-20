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
package com.blackducksoftware.integration.hub.nexus.repository.task.walker;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.nexus.event.HubEvent;
import com.blackducksoftware.integration.hub.nexus.event.handler.HubEventHandler;
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor;
import com.synopsys.integration.exception.IntegrationException;

public abstract class RepositoryWalkerProcessor<E extends HubEvent> extends AbstractWalkerProcessor {
    private final Logger logger = Loggers.getLogger(getClass());
    protected final ParallelEventProcessor parallelEventProcessor;

    public RepositoryWalkerProcessor(final ParallelEventProcessor parallelEventProcessor) {
        this.parallelEventProcessor = parallelEventProcessor;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        try {
            logger.info("Item pending scan {}", item);
            final HubEventHandler<E> hubEventHandler = getHubEventHandler(context, item);
            parallelEventProcessor.executeHandlerAndWaitForThread(hubEventHandler);
        } catch (final IntegrationException ex) {
            logger.error("Error occurred in walker processor for repository: ", ex);
        }
    }

    public abstract HubEventHandler<E> getHubEventHandler(final WalkerContext context, final StorageItem item) throws IntegrationException;

}
