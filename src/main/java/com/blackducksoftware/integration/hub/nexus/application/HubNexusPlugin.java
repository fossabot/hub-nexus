/*
 * hub-nexus
 *
 * 	Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.nexus.application;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.sisu.EagerSingleton;
import org.jetbrains.annotations.NonNls;
import org.sonatype.nexus.plugin.PluginIdentity;

@Named
@EagerSingleton
public class HubNexusPlugin extends PluginIdentity {

    @NonNls
    public static final String ID_PREFIX = "hub-nexus";

    @NonNls
    public static final String GROUP_ID = "com.blackducksoftware.integration";

    // The UI JS file must start with this artifact ID (The JS file has additional details after this name).

    @Inject
    public HubNexusPlugin() throws Exception {
        super(GROUP_ID, ID_PREFIX);
    }
}
