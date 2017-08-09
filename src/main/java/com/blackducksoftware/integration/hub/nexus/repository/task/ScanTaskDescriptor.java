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

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.PasswordFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

@Named(ScanTaskDescriptor.ID)
@Singleton
public class ScanTaskDescriptor extends AbstractScheduledTaskDescriptor {
    private static final String DEFAULT_FILE_PATTERNS = "*.war,*.zip,*.tar.gz,*.hpi";
    private static final String DEFAULT_WORKING_DIRECTORY = "/sonatype-work";
    private static final String DEFAULT_HUB_TIMEOUT = "300";
    public static final String ID = "Hub Repository Scan";

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

        final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField(TaskField.REPOSITORY_FIELD_ID.getParameterKey(), RepoOrGroupComboFormField.DEFAULT_LABEL, "Type in the repository in which to run the task.",
                FormField.MANDATORY);
        final StringTextFormField resourceStorePathField = new StringTextFormField(TaskField.REPOSITORY_PATH_FIELD_ID.getParameterKey(), "Repository path",
                "Enter a repository path to run the task in recursively (ie. \"/\" for root or \"/org/apache\").", FormField.OPTIONAL);
        final StringTextFormField hubUrlField = new StringTextFormField(TaskField.HUB_URL.getParameterKey(), "Hub URL", "URL to your Blackduck hub", FormField.MANDATORY);
        final StringTextFormField usernameField = new StringTextFormField(TaskField.HUB_USERNAME.getParameterKey(), "Hub Username", "Username for your Blackduck hub account to properly connect", FormField.MANDATORY);
        final PasswordFormField passwordField = new PasswordFormField(TaskField.HUB_PASSWORD.getParameterKey(), "Hub Password", "Password for your Blackduck hub account to properly connect", FormField.MANDATORY);
        final StringTextFormField timeoutField = new StringTextFormField(TaskField.HUB_TIMEOUT.getParameterKey(), "Timeout", "The timeout in seconds for a request to the Blackduck Hub server", FormField.OPTIONAL)
                .withInitialValue(DEFAULT_HUB_TIMEOUT);
        final CheckboxFormField autoImportCert = new CheckboxFormField(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), "Auto Import Certs", "Auto-import Hub server certificate into keystore", FormField.OPTIONAL);

        final StringTextFormField proxyHostField = new StringTextFormField(TaskField.HUB_PROXY_HOST.getParameterKey(), "Proxy Host", "The hostname of the proxy to communicate with the Blackduck Hub", FormField.OPTIONAL);
        final StringTextFormField proxyPortField = new StringTextFormField(TaskField.HUB_PROXY_PORT.getParameterKey(), "Proxy Port", "Port to communicate with the proxy", FormField.OPTIONAL);
        final StringTextFormField proxyUsernameField = new StringTextFormField(TaskField.HUB_PROXY_USERNAME.getParameterKey(), "Proxy Username", "Username for your authenticated proxy", FormField.OPTIONAL);
        final PasswordFormField proxyPasswordField = new PasswordFormField(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), "Proxy Password", "Password for your authenticated proxy", FormField.OPTIONAL);

        final StringTextFormField filePatternField = new StringTextFormField(TaskField.FILE_PATTERNS.getParameterKey(), "File Pattern Matches", "The file pattern match wildcard to filter the artifacts scanned.", FormField.MANDATORY)
                .withInitialValue(DEFAULT_FILE_PATTERNS);
        final StringTextFormField workingDirectoryField = new StringTextFormField(TaskField.WORKING_DIRECTORY.getParameterKey(), "Working Directory",
                "The parent directory where the blackduck directory will be creating to contain temporary data for the scans", FormField.MANDATORY).withInitialValue(DEFAULT_WORKING_DIRECTORY);

        fields.add(repoField);
        fields.add(resourceStorePathField);
        fields.add(hubUrlField);
        fields.add(usernameField);
        fields.add(passwordField);
        fields.add(timeoutField);
        fields.add(autoImportCert);
        fields.add(proxyHostField);
        fields.add(proxyPortField);
        fields.add(proxyUsernameField);
        fields.add(proxyPasswordField);
        fields.add(filePatternField);
        fields.add(workingDirectoryField);

        return fields;
    }
}
