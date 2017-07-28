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
package com.blackducksoftware.integration.hub.nexus.application;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.TextAreaFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

@Named(HelloWorldTaskDescriptor.ID)
@Singleton
public class HelloWorldTaskDescriptor extends AbstractScheduledTaskDescriptor {
    public static final String ID = "NexusHelloWorld";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "HELLO WORLD!";
    }

    @Override
    public List<FormField> formFields() {
        final CheckboxFormField checkbox = new CheckboxFormField("cbId", "Checkbox test", "This field is for testing the check boxes", false);
        final TextAreaFormField textarea = new TextAreaFormField("taId", "text area test", "This field is for testing the text area", false);
        final List<FormField> list = new ArrayList<>();
        list.add(checkbox);
        list.add(textarea);
        return list;
    }
}
