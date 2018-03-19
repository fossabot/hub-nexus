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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.List;

import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

public abstract class AbstractHubTask extends AbstractNexusRepositoriesPathAwareTask<Object> {
    private final Walker walker;

    public AbstractHubTask(final Walker walker) {
        this.walker = walker;
    }

    public void walkRepositories(final List<WalkerContext> contextList) {
        for (final WalkerContext context : contextList) {
            try {
                walker.walk(context);
            } catch (final WalkerException walkerEx) {
                logger.error("Exception walking repository. ", walkerEx);
            }
        }
    }

}
