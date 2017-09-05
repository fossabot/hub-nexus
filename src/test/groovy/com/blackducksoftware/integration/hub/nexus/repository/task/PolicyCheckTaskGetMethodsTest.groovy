package com.blackducksoftware.integration.hub.nexus.repository.task

import static org.junit.Assert.assertEquals

import org.junit.Test

public class PolicyCheckTaskGetMethodsTest {

    @Test
    public void testGetAction( ) {
        PolicyCheckTask policyCheckTask = new PolicyCheckTask(null, null)
        assertEquals("BLACKDUCK_HUB_POLICY_CHECK", policyCheckTask.getAction())
    }

    @Test
    public void testGetMessage() {
        PolicyCheckTask policyCheckTask = new PolicyCheckTask(null, null)
        assertEquals("HUB-NEXUS-PLUGIN-POLICY-CHECK: Search for successfully scanned artifacts and check their policy", policyCheckTask.getMessage())
    }
}
