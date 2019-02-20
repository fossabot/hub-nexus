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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.PasswordFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;

public abstract class AbstractHubTaskDescriptor extends AbstractScheduledTaskDescriptor {

    public static final String DEFAULT_HUB_TIMEOUT = "300";
    private static final String DESCRIPTION_HUB_IMPORT_CERT = "Import the SSL Certificates from the specified HTTPS Hub Server. Note: For this to work, the keystore must be writable by the nexus user";
    private static final String DESCRIPTION_HUB_PASSWORD = "Provide the password to authenticate with your Hub server";
    private static final String DESCRIPTION_HUB_TIMEOUT = "The timeout in seconds for a request to the Blackduck Hub server";
    private static final String DESCRIPTION_HUB_URL = "Provide the URL that lets you access your Hub server. For example \"https://hub.example.com/\"";
    private static final String DESCRIPTION_HUB_USERNAME = "Provide the username to authenticate with your Hub server.";
    private static final String DESCRIPTION_PROXY_HOST = "The hostname of the proxy to communicate with your Hub server";
    private static final String DESCRIPTION_PROXY_PASSWORD = "Password for your authenticated proxy";
    private static final String DESCRIPTION_PROXY_PORT = "Port to communicate with the proxy";
    private static final String DESCRIPTION_PROXY_USERNAME = "Username for your authenticated proxy";

    private static final String LABEL_CONNECTION_TIMEOUT = "Connection Timeout";
    private static final String LABEL_HUB_PASSWORD = "Hub Password";
    private static final String LABEL_HUB_SERVER_URL = "Hub Server URL";
    private static final String LABEL_HUB_USERNAME = "Hub Username";
    private static final String LABEL_IMPORT_HUB_SSL_CERTIFICATE = "Import Hub SSL Certificate";

    private static final String LABEL_PROXY_HOST = "Proxy Host";
    private static final String LABEL_PROXY_PASSWORD = "Proxy Password";
    private static final String LABEL_PROXY_PORT = "Proxy Port";
    private static final String LABEL_PROXY_USERNAME = "Proxy Username";

    private final StringTextFormField hubUrlField = new StringTextFormField(TaskField.HUB_URL.getParameterKey(), LABEL_HUB_SERVER_URL, DESCRIPTION_HUB_URL, FormField.MANDATORY);
    private final StringTextFormField usernameField = new StringTextFormField(TaskField.HUB_USERNAME.getParameterKey(), LABEL_HUB_USERNAME, DESCRIPTION_HUB_USERNAME, FormField.MANDATORY);
    private final PasswordFormField passwordField = new PasswordFormField(TaskField.HUB_PASSWORD.getParameterKey(), LABEL_HUB_PASSWORD, DESCRIPTION_HUB_PASSWORD, FormField.MANDATORY);
    private final StringTextFormField timeoutField = new StringTextFormField(TaskField.HUB_TIMEOUT.getParameterKey(), LABEL_CONNECTION_TIMEOUT, DESCRIPTION_HUB_TIMEOUT, FormField.OPTIONAL).withInitialValue(DEFAULT_HUB_TIMEOUT);
    private final CheckboxFormField autoImportCert = new CheckboxFormField(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), LABEL_IMPORT_HUB_SSL_CERTIFICATE, DESCRIPTION_HUB_IMPORT_CERT, FormField.OPTIONAL);

    private final StringTextFormField proxyHostField = new StringTextFormField(TaskField.HUB_PROXY_HOST.getParameterKey(), LABEL_PROXY_HOST, DESCRIPTION_PROXY_HOST, FormField.OPTIONAL);
    private final StringTextFormField proxyPortField = new StringTextFormField(TaskField.HUB_PROXY_PORT.getParameterKey(), LABEL_PROXY_PORT, DESCRIPTION_PROXY_PORT, FormField.OPTIONAL);
    private final StringTextFormField proxyUsernameField = new StringTextFormField(TaskField.HUB_PROXY_USERNAME.getParameterKey(), LABEL_PROXY_USERNAME, DESCRIPTION_PROXY_USERNAME, FormField.OPTIONAL);
    private final PasswordFormField proxyPasswordField = new PasswordFormField(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), LABEL_PROXY_PASSWORD, DESCRIPTION_PROXY_PASSWORD, FormField.OPTIONAL);

    @SuppressWarnings("rawtypes")
    @Override
    public List<FormField> formFields() {
        final List<FormField> fields = new ArrayList<>();
        fields.add(hubUrlField);
        fields.add(usernameField);
        fields.add(passwordField);
        fields.add(timeoutField);
        fields.add(autoImportCert);
        fields.add(proxyHostField);
        fields.add(proxyPortField);
        fields.add(proxyUsernameField);
        fields.add(proxyPasswordField);

        return fields;
    }
}
