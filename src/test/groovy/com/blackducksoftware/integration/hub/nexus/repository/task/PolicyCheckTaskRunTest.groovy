package com.blackducksoftware.integration.hub.nexus.repository.task

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.repository.Repository

import com.blackducksoftware.integration.hub.nexus.test.TestEventBus
import com.blackducksoftware.integration.hub.nexus.test.TestWalker

public class PolicyCheckTaskRunTest {
    private TestWalker walker
    private TestEventBus eventBus

    @Before
    public void initTest() {
        walker = new TestWalker()
        eventBus = new TestEventBus()
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
        PolicyCheckTask policyCheckTask = new PolicyCheckTask(walker,null)
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
        PolicyCheckTask policyCheckTask = new PolicyCheckTask(walker,null)
        policyCheckTask.setRepositoryRegistry(registry)
        policyCheckTask.setEventBus(eventBus)
        RepositoryRegistry repositoryRegistry = policyCheckTask.getRepositoryRegistry()
        policyCheckTask.doRun()
        assertFalse(walker.hasContexts())
    }
}
