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
package com.blackducksoftware.integration.hub.nexus.event;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.model.PolicyStatusDescription;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@Named
@Singleton
public class HubPolicyCheckEventHandler extends HubEventHandler {

    @Inject
    public HubPolicyCheckEventHandler(final AttributesHandler attributesHandler) {
        super(attributesHandler);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle(final HubPolicyCheckEvent event) {
        final HubEventLogger logger = new HubEventLogger(event, LoggerFactory.getLogger(getClass()));
        try {
            logger.info("Begin checking policy event");
            final StorageItem item = event.getItem();

            final ProjectVersionView projectVersionView = event.getProjectVersionView();
            final Map<String, String> taskParameters = event.getTaskParameters();
            final HubServiceHelper hubServiceHelper = createServiceHelper(logger, taskParameters);
            if (hubServiceHelper != null) {
                final HubService hubService = hubServiceHelper.getHubResponseService();
                final VersionBomPolicyStatusView versionBomPolicyStatusView = hubService.getResponse(projectVersionView, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
                final PolicyStatusDescription policyCheckResults = new PolicyStatusDescription(versionBomPolicyStatusView);
                getAttributeHelper().setPolicyStatus(item, policyCheckResults.getPolicyStatusMessage());
                final String overallStatus = transformOverallStatus(versionBomPolicyStatusView.overallStatus.toString());
                getAttributeHelper().setOverallPolicyStatus(item, overallStatus);
            }
        } catch (final Exception ex) {
            logger.error("Error occurred checking policy", ex);
        } finally {
            logger.info("Finished checking policy event");
        }
    }

    private String transformOverallStatus(final String overallStatus) {
        String statusMessage = overallStatus;
        statusMessage = StringUtils.replace(overallStatus, "_", " ");
        statusMessage = StringUtils.lowerCase(statusMessage);
        statusMessage = StringUtils.capitaliseAllWords(statusMessage);
        return statusMessage;
    }
}
