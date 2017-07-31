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

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.templates.AbstractConfigurableTemplate;
import org.sonatype.nexus.templates.TemplateProvider;

public class HelloWorldTemplate extends AbstractConfigurableTemplate {

    public HelloWorldTemplate(final TemplateProvider provider, final String id, final String description) {
        super(provider, id, description);
    }

    @Override
    public Object create() throws ConfigurationException, IOException {
        return null;
    }

    @Override
    protected CoreConfiguration initCoreConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

}
