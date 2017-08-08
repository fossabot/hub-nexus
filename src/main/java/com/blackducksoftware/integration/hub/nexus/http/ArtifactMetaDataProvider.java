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
package com.blackducksoftware.integration.hub.nexus.http;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.rest.AbstractArtifactViewProvider;
import org.sonatype.nexus.rest.ArtifactViewProvider;

import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

@Named
@Singleton
@Component(role = ArtifactViewProvider.class, hint = "blackduck")
public class ArtifactMetaDataProvider extends AbstractArtifactViewProvider {
    private final DefaultAttributesHandler attributesHandler;

    @Inject
    public ArtifactMetaDataProvider(final DefaultAttributesHandler attributesHandler) {
        this.attributesHandler = attributesHandler;
    }

    @Override
    protected Object retrieveView(final ResourceStoreRequest resourceStoreRequest, final RepositoryItemUid repositoryItemUid, final StorageItem storageItem, final Request request) throws IOException {
        final MetaDataResponse metaDataResponse = new MetaDataResponse();
        final HubMetaData hubMetaData = new HubMetaData();

        final ItemAttributesHelper attHelper = new ItemAttributesHelper(attributesHandler);

        hubMetaData.setLastScanned(String.valueOf(attHelper.getAttributeLastScanned(storageItem)));
        hubMetaData.setRiskReportUrl(attHelper.getAttributeRiskReportUrl(storageItem));
        hubMetaData.setPolicyCheckResult(attHelper.getAttributePolicyResult(storageItem));

        metaDataResponse.setData(hubMetaData);
        return metaDataResponse;
    }

}
