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
package com.blackducksoftware.integration.hub.nexus.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class TestExecutorService extends AbstractExecutorService {
    private final List<Runnable> fakeList;
    private boolean isShutdown;

    public TestExecutorService() {
        fakeList = new ArrayList<>();
        isShutdown = false;
    }

    @Override
    public void shutdown() {
        fakeList.removeAll(fakeList);
        isShutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;
        return fakeList;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public void execute(final Runnable command) {
        fakeList.add(command);
    }

    public boolean hasItems() {
        return !fakeList.isEmpty();
    }

    public int getSize() {
        return fakeList.size();
    }

    public Runnable getItem(final int loc) {
        return fakeList.get(loc);
    }

}
