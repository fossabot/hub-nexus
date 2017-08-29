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
package com.blackducksoftware.integration.hub.nexus.http;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;

import com.blackducksoftware.integration.hub.nexus.helpers.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.helpers.TestingPropertyKey;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HubNexusRestResourceTestIT extends AbstractNexusTestEnvironment {
    private final RestConnectionTestHelper restConnection = new RestConnectionTestHelper();

    @Test
    public void getTest() throws IOException {
        final Gson gson = new Gson();
        final String restGetUrl = restConnection.getProperty(TestingPropertyKey.TEST_NEXUS_SERVER_URL) + "service/siesta/blackduck/info?repoId=releases&itemPath=fakepath/aura.sql/3.x/aura.sql-3.x.zip";
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(restGetUrl).build();

        final Response response = client.newCall(request).execute();
        final String responseBody = response.body().string();
        final TestJson testJson = gson.fromJson(responseBody, TestJson.class);
        Assert.assertTrue(testJson.scanStatus.equals("SUCCESS"));
    }

    class TestJson {
        String scanStatus;
        String policyStatus;
        String scanTime;
        String policyOverallStatus;
        String uiUrl;
    }
}
