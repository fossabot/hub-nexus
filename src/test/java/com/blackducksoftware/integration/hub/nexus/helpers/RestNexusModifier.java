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
package com.blackducksoftware.integration.hub.nexus.helpers;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestNexusModifier {
    private final String nexusServerUrl;
    private final String nexusPassword;
    private final String nexusUsername;

    private final OkHttpClient httpClient = new OkHttpClient();

    public RestNexusModifier(final RestConnectionTestHelper restConnectionTestHelper) {
        nexusServerUrl = restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_NEXUS_SERVER_URL);
        nexusPassword = restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_NEXUS_PASSWORD);
        nexusUsername = restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_NEXUS_USERNAME);
    }

    public void addArtifact() {
        final String repo = "testRepo";
        final String hasPom = "false";
        final String extension = "jar";
        final String group = "com.test";
        final String artifact = "test";
        final String version = "0.0.1";
        final String file = "test-0.0.1.jar";
        final String user = nexusUsername + ":" + nexusPassword;

        final Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(nexusServerUrl + "service/local/artifact/maven/content");
        requestBuilder.addHeader("r", repo);
        requestBuilder.addHeader("hasPom", hasPom);
        requestBuilder.addHeader("e", extension);
        requestBuilder.addHeader("g", group);
        requestBuilder.addHeader("a", artifact);
        requestBuilder.addHeader("v", version);
        requestBuilder.addHeader("p", extension);
        requestBuilder.addHeader("file", file);
        requestBuilder.addHeader("u", user);

        final Request request = requestBuilder.build();
        try {
            makeCall(request);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void addRepository(final String repositoryBody) {
    }

    public void deleteArtifact(final String path) {

    }

    public void deleteRepository(final String repoId) {

    }

    public Response makeCall(final Request request) throws IOException {
        return httpClient.newCall(request).execute();
    }
}
