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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.PasswordFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;

@Named
@Singleton
public class ScanTaskDescriptor extends AbstractScheduledTaskDescriptor {
    // This ID string must match the class name of the task that actually performs the opertaion
    public static final String ID = "ScanTask";
    public static final String PLUGIN_VERSION = "0.0.1-SNAPSHOT";
    public static final String BLACKDUCK_DIRECTORY = "blackduck";
    public static final String TASK_NAME = "Hub Repository Scan";
    private static final String DEFAULT_FILE_PATTERNS = "*.war,*.zip,*.tar.gz,*.hpi";
    private static final String DEFAULT_HUB_TIMEOUT = "300";
    private static final String DEFAULT_SCAN_MEMORY = "4096";
    private static final String DEFAULT_WORKING_DIRECTORY = "/sonatype-work";

    private static final String DESCRIPTION_HUB_IMPORT_CERT = "Import the SSL Certificates from the specified HTTPS Hub Server. Note: For this to work, the keystore must be writable by the nexus user";
    private static final String DESCRIPTION_HUB_PASSWORD = "Provide the password to authenticate with your Hub server";
    private static final String DESCRIPTION_HUB_PROJECT_DISTRIBUTION = "The default distribution setting applied to the project verion if the project version is created";
    private static final String DESCRIPTION_HUB_PROJECT_PHASE = "The default phase setting applied to the project verion if the project version is created";
    private static final String DESCRIPTION_HUB_SCAN_MEMORY = "Specify the memory, in megabytes, you would like to allocate for the BlackDuck Scan. Default: 4096";
    private static final String DESCRIPTION_HUB_TIMEOUT = "The timeout in seconds for a request to the Blackduck Hub server";
    private static final String DESCRIPTION_HUB_URL = "Provide the URL that lets you access your Hub server. For example \"https://hub.example.com/\"";
    private static final String DESCRIPTION_HUB_USERNAME = "Provide the username to authenticate with your Hub server.";
    private static final String DESCRIPTION_PROXY_HOST = "The hostname of the proxy to communicate with your Hub server";
    private static final String DESCRIPTION_PROXY_PASSWORD = "Password for your authenticated proxy";
    private static final String DESCRIPTION_PROXY_PORT = "Port to communicate with the proxy";
    private static final String DESCRIPTION_PROXY_USERNAME = "Username for your authenticated proxy";
    private static final String DESCRIPTION_REPO_NAME = "Type in the repository in which to run the task.";
    private static final String DESCRIPTION_REPO_PATH = "Enter a repository path to run the task in recursively (ie. \"/\" for root or \"/org/apache\").";
    private static final String DESCRIPTION_SCAN_FILE_PATTERN_MATCH = "The file pattern match wildcard to filter the artifacts scanned.";
    private static final String DESCRIPTION_TASK_WORKING_DIRECTORY = "The parent directory where the blackduck directory will be created to contain temporary data for the scans";
    private static final String DESCRIPTION_SCAN_CUTOFF_DAYS = "Check artifacts that were added sooner than the number of days listed here";
    private static final String DESCRIPTION_RESCAN_FAILURE = "Re-scan artifacts if the previous scan status was failed";

    private static final String LABEL_CONNECTION_TIMEOUT = "Connection Timeout";
    private static final String LABEL_DISTRIBUTION = "Distribution";
    private static final String LABEL_FILE_PATTERN_MATCHES = "File Pattern Matches";
    private static final String LABEL_HUB_PASSWORD = "Hub Password";
    private static final String LABEL_HUB_SERVER_URL = "Hub Server URL";
    private static final String LABEL_HUB_USERNAME = "Hub Username";
    private static final String LABEL_IMPORT_HUB_SSL_CERTIFICATE = "Import Hub SSL Certificate";
    private static final String LABEL_PHASE = "Phase ";
    private static final String LABEL_PROXY_HOST = "Proxy Host";
    private static final String LABEL_PROXY_PASSWORD = "Proxy Password";
    private static final String LABEL_PROXY_PORT = "Proxy Port";
    private static final String LABEL_PROXY_USERNAME = "Proxy Username";
    private static final String LABEL_REPO_PATH = "Repository Path";
    private static final String LABEL_SCAN_MEMORY_ALLOCATION = "Scan Memory Allocation";
    private static final String LABEL_WORKING_DIRECTORY = "Working Directory";
    private static final String LABEL_ARTIFACT_CUTOFF = "Scan number of days back";
    private static final String LABEL_RESCAN_FAILURE = "Re-scan Failed Attempts";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField(TaskField.REPOSITORY_FIELD_ID.getParameterKey(), RepoOrGroupComboFormField.DEFAULT_LABEL, DESCRIPTION_REPO_NAME, FormField.MANDATORY);
    private final StringTextFormField resourceStorePathField = new StringTextFormField(TaskField.REPOSITORY_PATH_FIELD_ID.getParameterKey(), LABEL_REPO_PATH, DESCRIPTION_REPO_PATH, FormField.OPTIONAL);
    private final StringTextFormField hubUrlField = new StringTextFormField(TaskField.HUB_URL.getParameterKey(), LABEL_HUB_SERVER_URL, DESCRIPTION_HUB_URL, FormField.MANDATORY);
    private final StringTextFormField usernameField = new StringTextFormField(TaskField.HUB_USERNAME.getParameterKey(), LABEL_HUB_USERNAME, DESCRIPTION_HUB_USERNAME, FormField.MANDATORY);
    private final PasswordFormField passwordField = new PasswordFormField(TaskField.HUB_PASSWORD.getParameterKey(), LABEL_HUB_PASSWORD, DESCRIPTION_HUB_PASSWORD, FormField.MANDATORY);
    private final StringTextFormField timeoutField = new StringTextFormField(TaskField.HUB_TIMEOUT.getParameterKey(), LABEL_CONNECTION_TIMEOUT, DESCRIPTION_HUB_TIMEOUT, FormField.OPTIONAL).withInitialValue(DEFAULT_HUB_TIMEOUT);
    private final CheckboxFormField autoImportCert = new CheckboxFormField(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), LABEL_IMPORT_HUB_SSL_CERTIFICATE, DESCRIPTION_HUB_IMPORT_CERT, FormField.OPTIONAL);
    private final NumberTextFormField artifactCutoffField = new NumberTextFormField(TaskField.OLD_ARTIFACT_CUTOFF.getParameterKey(), LABEL_ARTIFACT_CUTOFF, DESCRIPTION_SCAN_CUTOFF_DAYS, FormField.OPTIONAL).withInitialValue(365);

    private final StringTextFormField proxyHostField = new StringTextFormField(TaskField.HUB_PROXY_HOST.getParameterKey(), LABEL_PROXY_HOST, DESCRIPTION_PROXY_HOST, FormField.OPTIONAL);
    private final StringTextFormField proxyPortField = new StringTextFormField(TaskField.HUB_PROXY_PORT.getParameterKey(), LABEL_PROXY_PORT, DESCRIPTION_PROXY_PORT, FormField.OPTIONAL);
    private final StringTextFormField proxyUsernameField = new StringTextFormField(TaskField.HUB_PROXY_USERNAME.getParameterKey(), LABEL_PROXY_USERNAME, DESCRIPTION_PROXY_USERNAME, FormField.OPTIONAL);
    private final PasswordFormField proxyPasswordField = new PasswordFormField(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), LABEL_PROXY_PASSWORD, DESCRIPTION_PROXY_PASSWORD, FormField.OPTIONAL);
    private final StringTextFormField distributionFormField = new StringTextFormField(TaskField.DISTRIBUTION.getParameterKey(), LABEL_DISTRIBUTION, DESCRIPTION_HUB_PROJECT_DISTRIBUTION, FormField.OPTIONAL)
            .withInitialValue(ProjectVersionDistributionEnum.EXTERNAL.name());
    private final StringTextFormField phaseFormField = new StringTextFormField(TaskField.PHASE.getParameterKey(), LABEL_PHASE, DESCRIPTION_HUB_PROJECT_PHASE, FormField.OPTIONAL).withInitialValue(ProjectVersionPhaseEnum.DEVELOPMENT.name());
    private final StringTextFormField filePatternField = new StringTextFormField(TaskField.FILE_PATTERNS.getParameterKey(), LABEL_FILE_PATTERN_MATCHES, DESCRIPTION_SCAN_FILE_PATTERN_MATCH, FormField.MANDATORY)
            .withInitialValue(DEFAULT_FILE_PATTERNS);
    private final StringTextFormField scanMemoryField = new StringTextFormField(TaskField.HUB_SCAN_MEMORY.getParameterKey(), LABEL_SCAN_MEMORY_ALLOCATION, DESCRIPTION_HUB_SCAN_MEMORY, FormField.OPTIONAL)
            .withInitialValue(DEFAULT_SCAN_MEMORY);

    private final CheckboxFormField rescanFailures = new CheckboxFormField(TaskField.RESCAN_FAILURES.getParameterKey(), LABEL_RESCAN_FAILURE, DESCRIPTION_RESCAN_FAILURE, FormField.OPTIONAL);

    private final ApplicationDirectories appDirectories;

    @Inject
    public ScanTaskDescriptor(final ApplicationDirectories appDirectories) {
        this.appDirectories = appDirectories;
    }

    @Override
    public List<FormField> formFields() {
        final List<FormField> fields = new ArrayList<>();

        StringTextFormField workingDirectoryField;
        try {
            workingDirectoryField = new StringTextFormField(TaskField.WORKING_DIRECTORY.getParameterKey(), LABEL_WORKING_DIRECTORY, DESCRIPTION_TASK_WORKING_DIRECTORY, FormField.MANDATORY)
                    .withInitialValue(appDirectories.getWorkDirectory().getCanonicalPath());
        } catch (final IOException | NullPointerException e) {
            workingDirectoryField = new StringTextFormField(TaskField.WORKING_DIRECTORY.getParameterKey(), LABEL_WORKING_DIRECTORY, DESCRIPTION_TASK_WORKING_DIRECTORY, FormField.MANDATORY).withInitialValue(DEFAULT_WORKING_DIRECTORY);
        }

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
        fields.add(distributionFormField);
        fields.add(phaseFormField);
        fields.add(scanMemoryField);
        fields.add(rescanFailures);
        fields.add(filePatternField);
        fields.add(workingDirectoryField);
        fields.add(artifactCutoffField);

        return fields;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }
}
