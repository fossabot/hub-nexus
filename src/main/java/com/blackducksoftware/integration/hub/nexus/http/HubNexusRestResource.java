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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.common.Loggers;
import org.sonatype.sisu.siesta.common.Resource;

@Path(HubNexusRestResource.RESOURCE_PATH)
@Named
@Singleton
public class HubNexusRestResource extends ComponentSupport implements Resource {
    public static final String RESOURCE_PATH = "/blackduck/info";

    final Logger logger = Loggers.getLogger(HubNexusRestResource.class);

    @GET
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public List<HubMetaData> get() {
        final HubMetaData data = new HubMetaData();

        data.setScanTime("Get lastScanned");
        data.setPolicyStatus("GET POLCIY");
        data.setApiUrl("GET REEEEEEEE");

        final List<HubMetaData> allData = new ArrayList<>();
        allData.add(data);

        return allData;
    }

}
