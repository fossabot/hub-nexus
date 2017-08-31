package com.blackducksoftware.integration.hub.nexus.event.policy;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.events.Event;

import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent;
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEventHandler;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public class InViolationPolicyCheckEventIT extends AbstractPolicyCheckTest {

    @Override
    public String getZipFilePath() {
        return "src/test/resources/repo1/hub-teamcity-3.1.0.zip";
    }

    @Test
    public void testHandleEvent() throws Exception {
        final HubPolicyCheckEventHandler policyEventHandler = new HubPolicyCheckEventHandler(getAttributesHandler());

        for (final Event<?> event : getEventBus().getEvents()) {
            if (event instanceof HubPolicyCheckEvent) {
                final HubPolicyCheckEvent scanEvent = (HubPolicyCheckEvent) event;
                policyEventHandler.handle(scanEvent);
                Assert.assertTrue(getEventBus().hasEvents());
                final ItemAttributesHelper itemAttributesHelper = new ItemAttributesHelper(getAttributesHandler());
                final String overallStatus = itemAttributesHelper.getOverallPolicyStatus(getItem());
                final String policyMessage = itemAttributesHelper.getPolicyStatus(getItem());
                Assert.assertNotNull(overallStatus);
                Assert.assertNotNull(policyMessage);
                Assert.assertEquals("IN_VIOLATION", overallStatus);
                Assert.assertEquals("The Hub found: 1 components in violation, 0 components in violation, but overridden, and 19 components not in violation.", policyMessage);
            }
        }
    }
}
