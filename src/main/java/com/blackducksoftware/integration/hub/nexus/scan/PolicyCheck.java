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
package com.blackducksoftware.integration.hub.nexus.scan;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRequestService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;

@Named
@Singleton
public class PolicyCheck {
    private final Logger logger = Loggers.getLogger(PolicyCheck.class);

    private void waitForPolicyCheck(final CodeLocationRequestService codeLocationRequestService, final MetaService metaService, final ScanSummaryRequestService scanSummaryRequestService, final ScanStatusDataService scanStatusDataService,
            final ProjectVersionView version) {
        try {
            final List<CodeLocationView> allCodeLocations = codeLocationRequestService.getAllCodeLocationsForProjectVersion(version);
            logger.info("Checking policy of + " + allCodeLocations.size() + " code location's");
            final List<ScanSummaryView> scanSummaryViews = new ArrayList<>();
            for (final CodeLocationView codeLocationView : allCodeLocations) {
                final String scansLink = metaService.getFirstLinkSafely(codeLocationView, MetaService.SCANS_LINK);
                final List<ScanSummaryView> codeLocationScanSummaryViews = scanSummaryRequestService.getAllScanSummaryItems(scansLink);
                scanSummaryViews.addAll(codeLocationScanSummaryViews);
            }
            logger.info("Checking scan policy");
            scanStatusDataService.assertScansFinished(scanSummaryViews);
            logger.info("Policy check completed");
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
        }
    }

    public PolicyStatusDescription checkPolicyStatus(final CodeLocationRequestService codeLocationRequestService, final MetaService metaService, final ScanSummaryRequestService scanSummaryRequestService,
            final ScanStatusDataService scanStatusDataService, final ProjectVersionView version, final PolicyStatusDataService policyStatusDataService) {
        try {
            waitForPolicyCheck(codeLocationRequestService, metaService, scanSummaryRequestService, scanStatusDataService, version);
            final VersionBomPolicyStatusView versionBomPolicyStatusView = policyStatusDataService.getPolicyStatusForVersion(version);
            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(versionBomPolicyStatusView);
            return policyStatusDescription;
        } catch (final IntegrationException e) {
            throw new RuntimeException(e);
        }
    }
}
