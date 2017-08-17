/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.nexus.repository.task;

import org.junit.Assert;
import org.junit.Test;

public class ScanTaskDescriptorTest {

    @Test
    public void getIdTest() {
        final ScanTaskDescriptor taskDesc = new ScanTaskDescriptor();
        Assert.assertSame("ScanTask", taskDesc.getId());
    }

    @Test
    public void getNameTest() {
        final ScanTaskDescriptor taskDesc = new ScanTaskDescriptor();
        Assert.assertSame("Hub Repository Scan", taskDesc.getName());
    }

    @Test
    public void formFieldsTest() {
        final ScanTaskDescriptor taskDesc = new ScanTaskDescriptor();
        Assert.assertTrue(taskDesc.formFields().size() == 17);
    }
}
