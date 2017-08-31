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
package com.blackducksoftware.integration.hub.nexus.test

import org.sonatype.nexus.proxy.attributes.Attributes

public class MockAttributes implements Attributes {
    private final Map<String, String> attributes = new HashMap<>()

    @Override
    public Map<String, String> asMap() {
        return attributes
    }

    @Override
    public boolean containsKey(final String key) {
        return attributes.containsKey(key)
    }

    @Override
    public String get(final String key) {
        return attributes.get(key)
    }

    @Override
    public String put(final String key, final String value) {
        return attributes.put(key, value)
    }

    @Override
    public String remove(final String key) {
        return attributes.remove(key)
    }

    @Override
    public void putAll(final Map<? extends String, ? extends String> map) {
    }

    @Override
    public void overlayAttributes(final Attributes repositoryItemAttributes) {
    }

    @Override
    public int getGeneration() {
        return 0
    }

    @Override
    public void setGeneration(final int value) {
    }

    @Override
    public void incrementGeneration() {
    }

    @Override
    public String getPath() {
        return null
    }

    @Override
    public void setPath(final String value) {
    }

    @Override
    public boolean isReadable() {
        return false
    }

    @Override
    public void setReadable(final boolean value) {
    }

    @Override
    public boolean isWritable() {
        return false
    }

    @Override
    public void setWritable(final boolean value) {

    }

    @Override
    public String getRepositoryId() {
        return null
    }

    @Override
    public void setRepositoryId(final String value) {
    }

    @Override
    public long getCreated() {
        return 0
    }

    @Override
    public void setCreated(final long value) {
    }

    @Override
    public long getModified() {
        return 0
    }

    @Override
    public void setModified(final long value) {
    }

    @Override
    public long getStoredLocally() {
        return 0
    }

    @Override
    public void setStoredLocally(final long value) {
    }

    @Override
    public long getCheckedRemotely() {
        return 0
    }

    @Override
    public void setCheckedRemotely(final long value) {
    }

    @Override
    public long getLastRequested() {
        return 0
    }

    @Override
    public void setLastRequested(final long value) {
    }

    @Override
    public boolean isExpired() {
        return false
    }

    @Override
    public void setExpired(final boolean value) {
    }

    @Override
    public String getRemoteUrl() {
        return null
    }

    @Override
    public void setRemoteUrl(final String value) {
    }
}
