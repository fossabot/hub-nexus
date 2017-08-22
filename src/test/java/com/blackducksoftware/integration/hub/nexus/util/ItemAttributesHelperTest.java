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
package com.blackducksoftware.integration.hub.nexus.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;

public class ItemAttributesHelperTest {

    private final AttributesHandler attributesHandler;
    private final StorageItem item;
    private final Attributes mockedAttributes;

    public ItemAttributesHelperTest() {
        attributesHandler = mock(AttributesHandler.class);

        mockedAttributes = mock(Attributes.class);

        item = mock(StorageItem.class);
        when(item.getRepositoryItemAttributes()).thenReturn(mockedAttributes);
    }

    // @Test
    // public void containsTest() throws IOException {
    // final String key = "blackduck-test";
    // final String expected = "Expected";
    //
    // item.getRepositoryItemAttributes().put(key, expected);
    //
    // final ItemAttributesHelper attHelper = new ItemAttributesHelper(attributesHandler);
    //
    // Assert.assertTrue(attHelper.contains(key, item));
    // }
    //
    // @Test
    // public void addAttributeTest() throws IOException {
    // final ItemAttributesHelper attHelper = new ItemAttributesHelper(attributesHandler);
    // final String key = "key";
    // final String value = "expected";
    // attHelper.addAttribute(key, value, item);
    //
    // Assert.assertNotNull(item.getRepositoryItemAttributes().get("blackduck-key"));
    // }

}
