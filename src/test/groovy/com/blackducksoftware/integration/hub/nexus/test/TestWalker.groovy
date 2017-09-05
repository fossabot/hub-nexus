package com.blackducksoftware.integration.hub.nexus.test

import org.sonatype.nexus.proxy.walker.Walker
import org.sonatype.nexus.proxy.walker.WalkerContext
import org.sonatype.nexus.proxy.walker.WalkerException

public class TestWalker implements Walker {
    private List<WalkerContext> contextList = new ArrayList<>()

    @Override
    public void walk(WalkerContext context) throws WalkerException {
        contextList.add(context)
    }

    public boolean hasContexts() {
        return !contextList.isEmpty()
    }

    public List<WalkerContext> getContextList() {
        return contextList;
    }
}
