/*
 * hub-nexus
 *
 * 	Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.nexus.event.handler;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.PolicyStatusDescription;

@Named
@Singleton
public class HubPolicyCheckEventHandler extends HubEventHandler<HubPolicyCheckEvent> {

    @Inject
    public HubPolicyCheckEventHandler(final ItemAttributesHelper itemAttributesHelper, final HubPolicyCheckEvent event, final HubServiceHelper hubServiceHelper) {
        super(itemAttributesHelper, event, hubServiceHelper);
    }

    private String transformOverallStatus(final String overallStatus) {
        String statusMessage = overallStatus;
        statusMessage = StringUtils.replace(overallStatus, "_", " ");
        statusMessage = StringUtils.lowerCase(statusMessage);
        statusMessage = StringUtils.capitaliseAllWords(statusMessage);
        return statusMessage;
    }

    @Override
    public void run() {
        final HubEventLogger logger = new HubEventLogger(getEvent(), LoggerFactory.getLogger(getClass()));
        try {
            logger.info("Begin checking policy event");
            final StorageItem item = getEvent().getItem();
            final ProjectVersionView projectVersionView = getEvent().getProjectVersionView();
            final HubServiceHelper hubServiceHelper = getHubServiceHelper();
            if (hubServiceHelper != null) {
                final BlackDuckService blackDuckService = hubServiceHelper.createBlackDuckServicesFactory().createBlackDuckService();
                final Optional<VersionBomPolicyStatusView> response = blackDuckService.getResponse(projectVersionView, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
                response.ifPresent(versionBomPolicyStatusView -> {
                    final PolicyStatusDescription policyCheckResults = new PolicyStatusDescription(versionBomPolicyStatusView);
                    getAttributeHelper().setPolicyStatus(item, policyCheckResults.getPolicyStatusMessage());
                    final String overallStatus = transformOverallStatus(versionBomPolicyStatusView.getOverallStatus().toString());
                    getAttributeHelper().setOverallPolicyStatus(item, overallStatus);
                });
            }
        } catch (final Exception ex) {
            logger.error("Error occurred checking policy", ex);
        } finally {
            logger.info("Finished checking policy event");
        }
    }

}
