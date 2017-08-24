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
package com.blackducksoftware.integration.hub.nexus.event;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HubPolicyCheckEventHandler.class)
public class HubPolicyCheckEventHandlerTest {

    @Mock
    HubPolicyCheckEvent hubPolicyCheckEvent;

    @Mock
    StorageItem item;

    @Mock
    ProjectVersionView projectVersionView;

    @Mock
    HubServiceHelper hubServiceHelper;

    @Mock
    HubEventLogger hubEventLogger;

    @Mock
    PolicyStatusDescription policyStatusDescription;

    @Test
    public void handleTest() throws IntegrationException {
        final HubPolicyCheckEventHandler hubPolicyCheckEventHandler = mock(HubPolicyCheckEventHandler.class);

        when(hubPolicyCheckEvent.getItem()).thenReturn(item);
        when(hubPolicyCheckEvent.getProjectVersionView()).thenReturn(projectVersionView);

        doReturn(hubServiceHelper).when((HubEventHandler) hubPolicyCheckEventHandler).createServiceHelper(hubEventLogger, new HashMap<String, String>());

        when(hubServiceHelper.checkPolicyStatus(projectVersionView)).thenReturn(policyStatusDescription);

        hubPolicyCheckEventHandler.handle(hubPolicyCheckEvent);

        final VersionBomPolicyStatusView policyStatus = hubServiceHelper.getOverallPolicyStatus(projectVersionView);
        // assertEquals(policyStatus.overallStatus.toString(), itemPolicy);
        // verify(hubServiceHelper).getOverallPolicyStatus(projectVersionView);
    }

}
