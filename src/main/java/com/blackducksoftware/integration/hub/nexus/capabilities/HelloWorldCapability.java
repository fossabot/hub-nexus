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
package com.blackducksoftware.integration.hub.nexus.capabilities;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.Condition;

@Named(HelloWorldCapabilityDescriptor.TYPE_ID)
public class HelloWorldCapability extends CapabilitySupport<HelloWorldCapabilityConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldCapability.class);

    @Inject
    public HelloWorldCapability() {
        log.info("capability ctor");
    }

    @Override
    protected HelloWorldCapabilityConfiguration createConfig(final Map<String, String> config) throws Exception {
        log.info("create config");
        return new HelloWorldCapabilityConfiguration(config);
    }

    @Override
    protected void onActivate(final HelloWorldCapabilityConfiguration config) throws Exception {
        log.info("OnActivate");
    }

    @Override
    protected void onPassivate(final HelloWorldCapabilityConfiguration config) throws Exception {
        log.info("OnPassivate");
    }

    @Override
    public Condition activationCondition() {
        log.info("capability activation condition");
        return conditions().capabilities().passivateCapabilityDuringUpdate();
    }
}
