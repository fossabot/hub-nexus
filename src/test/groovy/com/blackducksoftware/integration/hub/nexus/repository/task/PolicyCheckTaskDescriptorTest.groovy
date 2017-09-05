package com.blackducksoftware.integration.hub.nexus.repository.task

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import org.junit.Test

public class PolicyCheckTaskDescriptorTest {
    @Test
    public void getIdTest() {
        final PolicyCheckTaskDescriptor taskDesc = new PolicyCheckTaskDescriptor()
        assertEquals("PolicyCheckTask", taskDesc.getId())
    }

    @Test
    public void getNameTest() {
        final PolicyCheckTaskDescriptor taskDesc = new PolicyCheckTaskDescriptor()
        assertEquals("Hub Policy Check", taskDesc.getName())
    }

    @Test
    public void formFieldsTest() {
        final PolicyCheckTaskDescriptor taskDesc = new PolicyCheckTaskDescriptor()
        assertTrue(taskDesc.formFields().size() == 9)
    }
}
