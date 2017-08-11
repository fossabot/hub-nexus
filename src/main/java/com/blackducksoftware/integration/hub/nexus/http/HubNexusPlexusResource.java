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

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.nexus.rest.model.HtmlUnescapeStringConverter;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.sisu.goodies.common.Loggers;

import com.thoughtworks.xstream.XStream;

@Path(HubNexusPlexusResource.RESOURCE_PATH)
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
@Named("HubNexusPlexusResource")
@Singleton
public class HubNexusPlexusResource extends AbstractResourceStoreContentPlexusResource {
    public static final String RESOURCE_PATH = "/blackduck";

    final Logger logger = Loggers.getLogger(HubNexusPlexusResource.class);

    public HubNexusPlexusResource() {
        super();

        logger.info("Creating resource");
    }

    @Override
    public Object getPayloadInstance() {
        return new HubMetaData();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection() {
        return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:*]");
    }

    @Override
    public String getResourceUri() {
        return RESOURCE_PATH;
    }

    @Override
    public void configureXStream(final XStream stream) {
        super.configureXStream(stream);

        logger.info("Configuring stream");
        stream.processAnnotations(MetaDataResponse.class);

        final HtmlUnescapeStringConverter unescapeStringConverter = new HtmlUnescapeStringConverter(true);

        stream.registerLocalConverter(HubMetaData.class, "lastScanned", unescapeStringConverter);
        stream.registerLocalConverter(HubMetaData.class, "riskReportUrl", unescapeStringConverter);
        stream.registerLocalConverter(HubMetaData.class, "policyCheckStatus", unescapeStringConverter);
    }

    @Override
    @PUT
    public Object put(final Context context, final Request request, final Response response, final Object payload) throws ResourceException {
        final MetaDataResponse hubResponse = (MetaDataResponse) payload;

        if (hubResponse.getData() == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Hub meta data missing from request");
        }

        final HubMetaData info = hubResponse.getData();
        info.setLastScanned("Put lastScanned");
        info.setPolicyCheckResult("PUT policy");
        info.setRiskReportUrl("PUT RTeisks apsdjalsjfskdf");

        hubResponse.setData(info);

        return hubResponse;

    }

    @Override
    @GET
    public Object get(final Context context, final Request request, final Response response, final Variant variant) throws ResourceException {
        final MetaDataResponse hubResponse = new MetaDataResponse();
        final HubMetaData data = new HubMetaData();

        data.setLastScanned("Get lastScanned");
        data.setPolicyCheckResult("GET POLCIY");
        data.setRiskReportUrl("GET REEEEEEEE");

        hubResponse.setData(data);

        return hubResponse;
    }

    @Override
    protected ResourceStore getResourceStore(final Request request) throws NoSuchResourceStoreException, ResourceException {

        return null;
    }
}
