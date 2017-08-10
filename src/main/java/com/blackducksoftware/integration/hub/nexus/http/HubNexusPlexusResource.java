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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.HtmlUnescapeStringConverter;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import com.thoughtworks.xstream.XStream;

@Path("/blackduck")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
@Named("hub-nexus")
@Singleton
public class HubNexusPlexusResource extends AbstractNexusPlexusResource {

    @Override
    public Object getPayloadInstance() {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection() {
        return new PathProtectionDescriptor(getResourceUri(), "");
    }

    @Override
    public String getResourceUri() {
        return "/blackduck";
    }

    @Override
    public void configureXStream(final XStream stream) {
        super.configureXStream(stream);
        configureStream(stream);
    }

    private XStream configureStream(final XStream stream) {
        stream.processAnnotations(MetaDataResponse.class);

        final HtmlUnescapeStringConverter unescapeStringConverter = new HtmlUnescapeStringConverter(true);

        stream.registerLocalConverter(HubMetaData.class, "lastScanned", unescapeStringConverter);
        stream.registerLocalConverter(HubMetaData.class, "riskReportUrl", unescapeStringConverter);
        stream.registerLocalConverter(HubMetaData.class, "policyCheckStatus", unescapeStringConverter);

        return stream;
    }
}
