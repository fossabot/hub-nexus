package com.blackducksoftware.integration.hub.nexus.event;

import org.junit.Assert;
import org.junit.Test;

import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;

public class ScanEventHandlerEventTestIT extends AbstractScanHandlerTest {

    @Test
    public void testHandleEvent() throws Exception {
        getTaskParameters().put(TaskField.WORKING_DIRECTORY.getParameterKey(), getWorkHomeDir().getCanonicalPath());
        getTaskParameters().put(TaskField.HUB_SCAN_MEMORY.getParameterKey(), "4096");
        getTaskParameters().put(TaskField.HUB_TIMEOUT.getParameterKey(), "300");
        final HubScanEvent event = new HubScanEvent(getRepository(), getItem(), getTaskParameters(), getResourceStoreRequest());
        final HubScanEventHandler eventHandler = new HubScanEventHandler(getAppConfiguration(), getEventBus(), getAttributesHandler(), getEventManager());
        eventHandler.handle(event);
        Assert.assertTrue(getEventBus().hasEvents());
        Assert.assertTrue(event.isProcessed());
    }
}
