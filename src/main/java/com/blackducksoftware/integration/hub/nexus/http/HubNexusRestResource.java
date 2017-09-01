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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.common.Loggers;
import org.sonatype.sisu.siesta.common.Resource;

import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

@Path(HubNexusRestResource.RESOURCE_PATH)
@Named
@Singleton
public class HubNexusRestResource extends ComponentSupport implements Resource {
    public static final String RESOURCE_PATH = "/blackduck/info";

    private final RepositoryRegistry repositoryRegistry;
    private final ItemAttributesHelper itemAttributesHelper;

    final Logger logger = Loggers.getLogger(HubNexusRestResource.class);

    @Inject
    public HubNexusRestResource(final RepositoryRegistry repoRegistry, final AttributesHandler defaultAttributesHandler) {
        this.repositoryRegistry = repoRegistry;
        itemAttributesHelper = new ItemAttributesHelper(defaultAttributesHandler);
    }

    @DELETE
    public void delete(@QueryParam("repoId") final String repoId, @QueryParam("itemPath") final String itemPath) {
        Repository repo = null;

        try {
            repo = repositoryRegistry.getRepository(repoId);
        } catch (final NoSuchRepositoryException e) {
            logger.error("Error retrieving repo {}", e);
        }

        if (repo != null) {
            StorageItem item = null;
            final ResourceStoreRequest request = new ResourceStoreRequest(itemPath);

            try {
                item = repo.retrieveItem(request);
            } catch (StorageException | AccessDeniedException | ItemNotFoundException | IllegalOperationException e) {
                logger.error("Error retrieving item {}", e);
            }

            if (item != null) {
                itemAttributesHelper.clearBlackduckAttributes(item);
            }
        }
    }

    @GET
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public HubMetaData get(@QueryParam("repoId") final String repoId, @QueryParam("itemPath") final String itemPath) {
        Repository repo = null;
        final HubMetaData data = new HubMetaData();

        try {
            repo = repositoryRegistry.getRepository(repoId);
        } catch (final NoSuchRepositoryException e) {
            logger.error("Error retrieving repo {}", e);
        }

        if (repo != null) {
            StorageItem item = null;
            final ResourceStoreRequest request = new ResourceStoreRequest(itemPath);

            try {
                item = repo.retrieveItem(request);
            } catch (StorageException | AccessDeniedException | ItemNotFoundException | IllegalOperationException e) {
                logger.error("Error retrieving item {}", e);
            }

            if (item != null) {
                data.setScanStatus(itemAttributesHelper.getScanResult(item));
                data.setPolicyStatus(itemAttributesHelper.getPolicyStatus(item));
                data.setPolicyOverallStatus(itemAttributesHelper.getOverallPolicyStatus(item));
                data.setScanTime(String.valueOf(itemAttributesHelper.getScanTime(item)));
                data.setUiUrl(itemAttributesHelper.getUiUrl(item));

                return data;
            }
        }

        return data;
    }

}
