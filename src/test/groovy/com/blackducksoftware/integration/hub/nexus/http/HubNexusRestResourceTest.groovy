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
package com.blackducksoftware.integration.hub.nexus.http

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

import org.apache.commons.lang3.StringUtils
import org.junit.Before
import org.junit.Test
import org.sonatype.nexus.proxy.attributes.AttributesHandler
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.repository.Repository

import com.blackducksoftware.integration.hub.nexus.test.MockAttributes

public class HubNexusRestResourceTest {
    private RepositoryRegistry repositoryRegistry;
    private Repository repository;
    private StorageItem item;
    private MockAttributes mockAttributes;
    private AttributesHandler attributesHandler;

    @Before
    public void createAttributes() {

        mockAttributes = new MockAttributes()
        mockAttributes.put("blackduck-scanTime", "100")
        mockAttributes.put("blackduck-scanResult", "1")
        mockAttributes.put("blackduck-apiUrl", "apiurl")
        mockAttributes.put("blackduck-uiUrl", "uiurl")
        mockAttributes.put("blackduck-policyStatus", "NOT_IN_VIOLATION")
        mockAttributes.put("blackduck-overallPolicyStatus", "policy message")
    }

    @Test
    public void getRepoNotFoundTest() throws IOException {
        repositoryRegistry = [getRepository: {repoId -> return null}] as RepositoryRegistry
        HubNexusRestResource restResource = new HubNexusRestResource(repositoryRegistry, attributesHandler)
        HubMetaData hubMetaData = restResource.get("","")
        assertNotNull(hubMetaData)
        assertTrue(StringUtils.isBlank(hubMetaData.apiUrl))
        assertTrue(StringUtils.isBlank(hubMetaData.uiUrl))
        assertTrue(StringUtils.isBlank(hubMetaData.scanTime))
        assertTrue(StringUtils.isBlank(hubMetaData.scanStatus))
        assertTrue(StringUtils.isBlank(hubMetaData.policyOverallStatus))
        assertTrue(StringUtils.isBlank(hubMetaData.policyStatus))
    }

    @Test
    public void getTest() throws IOException {
        item = [ getRepositoryItemAttributes: { -> return mockAttributes }] as StorageItem
        repository = [ retrieveItem: { itemPath -> return item }] as Repository
        repositoryRegistry = [ getRepository: { repoId -> return repository }] as RepositoryRegistry
        HubNexusRestResource restResource = new HubNexusRestResource(repositoryRegistry, attributesHandler)
        HubMetaData hubMetaData = restResource.get("","")
        assertNotNull(hubMetaData)
        assertEquals("uiurl", hubMetaData.uiUrl)
        assertEquals("100", hubMetaData.scanTime)
        assertEquals("1", hubMetaData.scanStatus)
        assertEquals("NOT_IN_VIOLATION", hubMetaData.policyStatus)
        assertEquals("policy message", hubMetaData.policyOverallStatus)
    }

    @Test
    public void deleteRepoNotFoundTest() throws Exception {
        attributesHandler = [ storeAttributes: { item -> return }] as AttributesHandler
        item = [ getRepositoryItemAttributes: { -> return mockAttributes }] as StorageItem
        repository = [ retrieveItem: { itemPath -> return item }] as Repository
        repositoryRegistry = [ getRepository: { repoId -> return repository }] as RepositoryRegistry
        HubNexusRestResource restResource = new HubNexusRestResource(repositoryRegistry, attributesHandler)
        restResource.delete("","")
        assertNull(mockAttributes.get("blackduck-scanTime"))
        assertNull(mockAttributes.get("blackduck-scanResult"))
        assertNull(mockAttributes.get("blackduck-apiUrl"))
        assertNull(mockAttributes.get("blackduck-uiUrl"))
        assertNull(mockAttributes.get("blackduck-policyStatus"))
        assertNull(mockAttributes.get("blackduck-overallPolicyStatus"))
    }
}
