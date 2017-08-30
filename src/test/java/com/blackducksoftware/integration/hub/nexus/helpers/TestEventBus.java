package com.blackducksoftware.integration.hub.nexus.helpers;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.events.Event;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.blackducksoftware.integration.hub.nexus.event.HubEvent;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;

public class TestEventBus implements EventBus {

    Map<String, Event<?>> eventMap = new HashMap<>();

    @Override
    public EventBus register(final Object handler) {
        return null;
    }

    @Override
    public EventBus unregister(final Object handler) {
        return null;
    }

    @Override
    public EventBus post(final Object event) {
        if (event instanceof HubEvent) {
            final HubEvent hubEvent = (HubEvent) event;
            eventMap.put(hubEvent.getEventId().toString(), hubEvent);
        }
        return this;
    }

    public boolean containsEvent(final HubScanEvent event) {
        return eventMap.containsKey(event.getEventId().toString());
    }

    public boolean hasEvents() {
        return !eventMap.isEmpty();
    }

    public int getEventCount() {
        return eventMap.size();
    }
}
