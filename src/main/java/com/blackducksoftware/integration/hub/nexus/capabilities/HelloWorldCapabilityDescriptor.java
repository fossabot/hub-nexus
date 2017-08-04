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

import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.blackducksoftware.integration.hub.nexus.application.HubNexusPlugin;
import com.google.common.collect.Lists;

@Named(HelloWorldCapabilityDescriptor.TYPE_ID)
@Singleton
public class HelloWorldCapabilityDescriptor extends CapabilityDescriptorSupport implements Taggable {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldCapabilityDescriptor.class);

    public static final String TYPE_ID = HubNexusPlugin.ARTIFACT_ID;

    public static final CapabilityType TYPE = CapabilityType.capabilityType(TYPE_ID);

    private static interface Messages extends MessageBundle {
        @DefaultMessage("Hello world capability")
        String name();
    }

    private static final Messages messages = I18N.create(Messages.class);

    private final List<FormField> formFields;

    public HelloWorldCapabilityDescriptor() {
        log.info("HW Cap Desc");
        formFields = Lists.<FormField> newArrayList(new StringTextFormField("tttt", "label", "help", FormField.MANDATORY));
    }

    @Override
    public List<FormField> formFields() {
        // final CheckboxFormField checkbox = new CheckboxFormField("cbId", "Checkbox test", "This field is for testing
        // the check boxes", false);
        // final TextAreaFormField textarea = new TextAreaFormField("taId", "text area test", "This field is for testing
        // the text area", false);
        // final List<FormField> list = new ArrayList<>();
        // list.add(checkbox);
        // list.add(textarea);
        // return list;
        return formFields;
    }

    @Override
    public String name() {
        // return messages.name();
        return "Paulo";
    }

    @Override
    public CapabilityType type() {
        return TYPE;
    }

    @Override
    public Validator validator() {
        // Allow only one capability of this type
        return validators().capability().uniquePer(HelloWorldCapabilityDescriptor.TYPE);
    }

    @Override
    public Set<Tag> getTags() {
        return Tag.tags(Tag.categoryTag("hwhwhwhwhw"));
    }

}
