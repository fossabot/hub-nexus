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
package com.blackducksoftware.integration.hub.nexus.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.nexus.plugin.support.UrlWebResource;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.nexus.web.WebResourceBundle;
import org.sonatype.sisu.goodies.common.Loggers;

@Named("HubPluginWebResourceBundle")
@Singleton
public class HubPluginWebResourceBundle implements WebResourceBundle {
    final Logger logger = Loggers.getLogger(HubPluginWebResourceBundle.class);
    public static final String HUB_TAB_RESOURCE_PATH = "js/blackduck/hub-tab.js";

    @Override
    public List<WebResource> getResources() {
        logger.info("Getting resource bundle");
        final List<WebResource> resources = new ArrayList<>();
        resources.add(new UrlWebResource(getClass().getResource("/js/hub-tab.js"), "/" + HUB_TAB_RESOURCE_PATH, "text/javascript"));
        return resources;
    }

}
