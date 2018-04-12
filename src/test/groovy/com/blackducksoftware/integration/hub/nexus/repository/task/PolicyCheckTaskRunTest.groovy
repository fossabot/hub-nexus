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
package com.blackducksoftware.integration.hub.nexus.repository.task

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.repository.Repository

import com.blackducksoftware.integration.hub.nexus.event.TaskEventManager
import com.blackducksoftware.integration.hub.nexus.test.TestEventBus
import com.blackducksoftware.integration.hub.nexus.test.TestWalker

public class PolicyCheckTaskRunTest {
    private TestWalker walker
    private TestEventBus eventBus
    private TaskWalker taskWalker
    private TaskEventManager taskEventManager

    @Before
    public void initTest() {
        walker = new TestWalker()
        eventBus = new TestEventBus()
        taskWalker = new TaskWalker(walker)
        taskEventManager = new TaskEventManager(eventBus)
    }

    private List<Repository> createRepositoryList(int count) {
        List<Repository> repositoryList = new ArrayList<>()
        for(int index = 0; index < count; index++) {
            Repository repository = [ getName: {-> "repository_"+count}] as Repository
            repositoryList.add(repository)
        }

        return repositoryList
    }

    @Test
    public void testWalkingContext() {
        int count = 2
        List<Repository> repositoryList = createRepositoryList(count)
        RepositoryRegistry registry = [ getRepositories: { -> repositoryList }] as RepositoryRegistry
        PolicyCheckTask policyCheckTask = new PolicyCheckTask(taskWalker,null, taskEventManager)
        policyCheckTask.setRepositoryRegistry(registry)
        policyCheckTask.setEventBus(eventBus)
        RepositoryRegistry repositoryRegistry = policyCheckTask.getRepositoryRegistry()
        policyCheckTask.doRun()
        assertTrue(walker.hasContexts())
        assertEquals(count, walker.getContextList().size())
    }

    @Test
    public void testNoRepos() {
        int count = 0
        List<Repository> repositoryList = createRepositoryList(count)
        RepositoryRegistry registry = [ getRepositories: { -> repositoryList }] as RepositoryRegistry
        PolicyCheckTask policyCheckTask = new PolicyCheckTask(taskWalker,null, taskEventManager)
        policyCheckTask.setRepositoryRegistry(registry)
        policyCheckTask.setEventBus(eventBus)
        RepositoryRegistry repositoryRegistry = policyCheckTask.getRepositoryRegistry()
        policyCheckTask.doRun()
        assertFalse(walker.hasContexts())
    }
}
