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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.capability.support.CapabilityConfigurationSupport;

import com.google.common.collect.Maps;

public class HelloWorldCapabilityConfiguration extends CapabilityConfigurationSupport {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldCapabilityConfiguration.class);

    private final String checkbox;

    private final String textarea;

    public HelloWorldCapabilityConfiguration(final Map<String, String> properties) {
        checkNotNull(properties);
        log.info("HW cap Cnofig");
        checkbox = properties.get("cbId");
        textarea = properties.get("taId");
    }

    public Map<String, String> asMap() {
        final Map<String, String> props = Maps.newHashMap();
        props.put("cbId", checkbox);
        props.put("taId", textarea);
        return props;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "saasdasdasdasd";
    }
}
