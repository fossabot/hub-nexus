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
package com.blackducksoftware.integration.hub.nexus.event.handler;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.blackducksoftware.integration.hub.nexus.repository.task.AbstractHubWalkerTask;
import com.blackducksoftware.integration.hub.nexus.repository.task.PolicyCheckTask;
import com.blackducksoftware.integration.hub.nexus.repository.task.ScanTask;
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class NexusTaskEventStoppedCanceledHandler extends ComponentSupport implements EventSubscriber {
    private final ParallelEventProcessor parallelEventProcessor;
    private final Set<Class<? extends AbstractHubWalkerTask>> taskNames;

    @Inject
    public NexusTaskEventStoppedCanceledHandler(final ParallelEventProcessor parallelEventProcessor) {
        this.parallelEventProcessor = parallelEventProcessor;
        taskNames = new HashSet<>();
        taskNames.add(ScanTask.class);
        taskNames.add(PolicyCheckTask.class);
    }

    @Subscribe
    public void handle(final NexusTaskEventStoppedCanceled<Object> nexusTaskEventStoppedCanceled) {
        if (isBlackduckTask(nexusTaskEventStoppedCanceled.getNexusTask())) {
            createLogger().info("Cancelling blackduck task safely");
            parallelEventProcessor.hardShutdown();
        }
    }

    private boolean isBlackduckTask(final NexusTask<?> task) {
        return taskNames.contains(task.getClass());
    }
}
