package com.blackducksoftware.integration.hub.nexus.repository.task

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import org.apache.commons.collections.map.HashedMap
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.sonatype.nexus.events.Event
import org.sonatype.nexus.proxy.attributes.Attributes
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.walker.WalkerContext

import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.event.HubPolicyCheckEvent
import com.blackducksoftware.integration.hub.nexus.test.TestEventBus
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper

@RunWith(MockitoJUnitRunner.class)
public class PolicyWalkerTest {

    private final static String PARENT_PATH="/test/0.0.1-SNAPSHOT"
    private final static String PROJECT_NAME="test"

    @Mock
    private ItemAttributesHelper itemAttributesHelper
    @Mock
    private HubServiceHelper hubServiceHelper
    private StorageItem item
    private WalkerContext walkerContext
    private RepositoryItemUid repositoryItemUid
    private Attributes attributes
    private TestEventBus eventBus
    private Map<String,String> taskParameters

    @Before
    public void initTest() {
        taskParameters = new HashedMap<>()
        taskParameters.put(TaskField.DISTRIBUTION.getParameterKey(), "EXTERNAL")
        taskParameters.put(TaskField.PHASE.getParameterKey(), "DEVELOPMENT")

        eventBus = new TestEventBus();
        repositoryItemUid = [ getBooleanAttributeValue: { attr -> false }, getRepository: { -> null } ] as RepositoryItemUid

        item = [ getRepositoryItemUid: { -> repositoryItemUid },
            getRemoteUrl: { -> "" },
            getPath: { -> PROJECT_NAME },
            getRepositoryItemAttributes: { -> attributes },
            getParentPath: { -> PARENT_PATH },
            getName: { -> "itemName" }] as StorageItem
        walkerContext = [ getResourceStoreRequest: { -> null } ] as WalkerContext
        ProjectVersionRequestService projectVersionRequestService = Mockito.mock(ProjectVersionRequestService.class)
        hubServiceHelper = Mockito.mock(HubServiceHelper.class)
        Mockito.when(hubServiceHelper.getProjectVersionRequestService()).thenReturn(projectVersionRequestService)
        Mockito.when(projectVersionRequestService.getItem(Mockito.anyString(), Mockito.any())).thenReturn(Mockito.mock(ProjectVersionView.class))
    }

    @Test
    public void testScanSuccess() {
        Mockito.when(itemAttributesHelper.getScanResult(item)).thenReturn(ItemAttributesHelper.SCAN_STATUS_SUCCESS)

        final PolicyRepositoryWalker walker = new PolicyRepositoryWalker(eventBus, itemAttributesHelper, taskParameters, hubServiceHelper);
        walker.processItem(walkerContext, item)
        assertEquals(1, eventBus.getEventCount())
        Collection<Event<?>> eventCollection = eventBus.getEvents()
        for(Event<?> event : eventCollection ) {
            assertTrue(event instanceof HubPolicyCheckEvent)
            HubPolicyCheckEvent policyEvent = (HubPolicyCheckEvent) event
            assertEquals(item, policyEvent.getItem())
        }
    }

    @Test
    public void testScanFailed() {
        Mockito.when(itemAttributesHelper.getScanResult(item)).thenReturn(ItemAttributesHelper.SCAN_STATUS_FAILED)

        final PolicyRepositoryWalker walker = new PolicyRepositoryWalker(eventBus, itemAttributesHelper, taskParameters, hubServiceHelper);
        walker.processItem(walkerContext, item)
        assertFalse(eventBus.hasEvents())
    }
}
