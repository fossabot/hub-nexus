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
package com.blackducksoftware.integration.hub.nexus.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.nexus.event.handler.HubEventHandler;

@Named
@Singleton
public class ParallelEventProcessor {
    private final Logger logger = Loggers.getLogger(getClass());
    private ExecutorService executorService;

    public ExecutorService initializeExecutorService() {
        return initializeExecutorService(Runtime.getRuntime().availableProcessors());
    }

    public ExecutorService initializeExecutorService(final int availableProcessors) {
        hardShutdown();
        executorService = createExecutorService(availableProcessors);
        return executorService;
    }

    public ExecutorService createExecutorService(final int availableProcessors) {
        logger.info("Using {} parallel processors", availableProcessors);
        return Executors.newFixedThreadPool(availableProcessors);
    }

    public void executeHandlerAndWaitForThread(final HubEventHandler<?> eventHandler) throws InterruptedException {
        if (executorService != null) {
            boolean runTask = true;
            while (runTask && !executorService.isShutdown()) {
                try {
                    executeHandler(eventHandler);
                    runTask = false;
                } catch (final RejectedExecutionException e) {
                    logger.info("Waiting for open thread");
                    Thread.sleep(5000);
                }
            }
        }
    }

    public void executeHandler(final HubEventHandler<?> eventHandler) {
        if (executorService != null) {
            executorService.execute(eventHandler);
        }
    }

    public void hardShutdown() {
        if (executorService != null) {
            shutdownProcessor();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.info("Attempting hard shutdown");
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.error("Threads did not terminate properly");
                    }
                }
            } catch (final InterruptedException ie) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                executorService = null;
            }
        }
    }

    public void shutdownProcessor() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

}
