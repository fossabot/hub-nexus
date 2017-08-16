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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.nexus.scan.HubServiceHelper;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class HubPolicyCheckEventHandler extends HubEventHandler {

    @Inject
    public HubPolicyCheckEventHandler(final DefaultAttributesHandler attributesHandler) {
        super(attributesHandler);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle(final HubPolicyCheckEvent event) {
        try {
            log.info("Policy Check Event Handler called for event {}", event);
            final StorageItem item = event.getItem();
            final ProjectVersionView projectVersionView = event.getProjectVersionView();
            final Map<String, String> taskParameters = event.getTaskParameters();
            final HubServerConfig hubServerConfig = createHubServerConfig(taskParameters);
            final HubServiceHelper hubServiceHelper = createServiceHelper(hubServerConfig);
            if (hubServiceHelper != null) {
                final PolicyStatusDescription policyCheckResults = hubServiceHelper.checkPolicyStatus(projectVersionView);
                if (policyCheckResults != null) {
                    final VersionBomPolicyStatusView versionBomPolicyStatusView = hubServiceHelper.getOverallPolicyStatus(projectVersionView);
                    getAttributeHelper().setPolicyStatus(item, policyCheckResults.getPolicyStatusMessage());
                    getAttributeHelper().setOverallPolicyStatus(item, versionBomPolicyStatusView.overallStatus.toString());
                }
            }
        } catch (final Exception ex) {
            log.error("Error occurred checking policy for event {}", event, ex);
        }
    }
}
