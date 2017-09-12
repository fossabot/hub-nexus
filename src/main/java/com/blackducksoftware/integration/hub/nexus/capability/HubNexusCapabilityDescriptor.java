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
package com.blackducksoftware.integration.hub.nexus.capability;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Validator;

import com.blackducksoftware.integration.hub.nexus.application.HubNexusPlugin;

@Named(HubNexusCapabilityDescriptor.ID)
@Singleton
public class HubNexusCapabilityDescriptor extends CapabilityDescriptorSupport implements CapabilityDescriptor {
    public static final String ID = HubNexusPlugin.ID_PREFIX;
    public static final CapabilityType TYPE = CapabilityType.capabilityType(ID);

    @Override
    public CapabilityType type() {
        return TYPE;
    }

    @Override
    public String name() {
        return "Blackduck hub plugin";
    }

    @Override
    public String about() {
        return "UHHH";
    }

    @Override
    public List<FormField> formFields() {
        final List<FormField> formFields = new ArrayList<>();

        formFields.add(new CheckboxFormField("id", "test", "test desc", FormField.OPTIONAL));

        return formFields;
    }

    @Override
    public Validator validator() {
        return validators().capability().uniquePer(TYPE);
    }

    @Override
    protected String renderAbout() throws Exception {
        return render(ID + "-about.vm");
    }

}
