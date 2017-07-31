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
package com.blackducksoftware.integration.hub.nexus.task;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

@Named(HelloWorldTaskDescriptor.ID)
public class HelloWorldTask extends AbstractNexusRepositoriesTask<Repository> {
    NexusConfiguration nexusConfig = null;

    @Inject
    public HelloWorldTask(final NexusConfiguration nexusConfiguration) {
        nexusConfig = nexusConfiguration;
    }

    @Override
    protected Repository doRun() throws Exception {
        final RepositoryRegistry repos = this.getRepositoryRegistry();
        final List<Repository> listOfRepos = repos.getRepositories();

        return listOfRepos.get(0);
    }

    @Override
    protected String getAction() {
        return "TEST";
    }

    @Override
    protected String getMessage() {
        return "Test for Nexus plugin";
    }

}
