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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.PasswordFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

@Named(ScanTaskDescriptor.ID)
@Singleton
public class ScanTaskDescriptor extends AbstractScheduledTaskDescriptor {
    public static final String ID = "Hub Repository Scan";
    public static final String REPOSITORY_FIELD_ID = "repositoryId";
    public static final String REPOSITORY_PATH_FIELD_ID = "repositoryPath";
    public static final String HUB_PASSWORD = "hubPassword";
    public static final String HUB_USERNAME = "hubUsername";
    public static final String HUB_URL = "hubUrl";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Hub Repository Scan";
    }

    @Override
    public List<FormField> formFields() {
        final List<FormField> fields = new ArrayList<>();

        final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField(REPOSITORY_FIELD_ID, RepoOrGroupComboFormField.DEFAULT_LABEL, "Type in the repository in which to run the task.", FormField.MANDATORY);
        final StringTextFormField resourceStorePathField = new StringTextFormField(REPOSITORY_PATH_FIELD_ID, "Repository path", "Enter a repository path to run the task in recursively (ie. \"/\" for root or \"/org/apache\").",
                FormField.OPTIONAL);
        final StringTextFormField hubUrlField = new StringTextFormField(HUB_URL, "Hub URL", "URL to your Blackduck hub", FormField.MANDATORY);
        final StringTextFormField usernameField = new StringTextFormField(HUB_USERNAME, "Hub username", "Username for your Blackduck hub account to properly connect", FormField.MANDATORY);
        final PasswordFormField passwordField = new PasswordFormField(HUB_PASSWORD, "Hub password", "Password for your Blackduck hub account to properly connect", FormField.MANDATORY);

        fields.add(repoField);
        fields.add(resourceStorePathField);
        fields.add(hubUrlField);
        fields.add(usernameField);
        fields.add(passwordField);

        return fields;
    }
}
