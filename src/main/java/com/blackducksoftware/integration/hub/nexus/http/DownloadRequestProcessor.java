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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.AbstractRequestStrategy;
import org.sonatype.nexus.proxy.repository.Repository;

import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

@Named
@Singleton
public class DownloadRequestProcessor extends AbstractRequestStrategy {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ItemAttributesHelper itemAttributesHelper;

    @Inject
    public DownloadRequestProcessor(final AttributesHandler attributesHandler) {
        logger.info("Request strat ctor");
        itemAttributesHelper = new ItemAttributesHelper(attributesHandler);
    }

    @Override
    public void onServing(final Repository repository, final ResourceStoreRequest request, final StorageItem item) throws ItemNotFoundException, IllegalOperationException {
        final String policyStatus = itemAttributesHelper.getOverallPolicyStatus(item);
        if (policyStatus.toLowerCase().equals("not in violation") && request.getRequestUrl() != null && !request.isDescribe()) {
            throw new IllegalStateException("Cannot serve a file in violation");
        }
    }
}
