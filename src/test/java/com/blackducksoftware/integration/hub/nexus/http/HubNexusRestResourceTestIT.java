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

import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;

public class HubNexusRestResourceTestIT extends AbstractNexusTestEnvironment {

    // private static final String BASE_URL = "http://localhost:8081/nexus/service/siesta/blackduck/info?repoId=releases&itemPath=fakepath/aura.sql/3.x/aura.sql-3.x.zip";
    //
    // private final RepositoryRegistry repoReg;
    // private final DefaultAttributesHandler attHandler;
    //
    // public HubNexusRestResourceTest() throws Exception {
    // repoReg = lookup(RepositoryRegistry.class);
    // attHandler = lookup(DefaultAttributesHandler.class);
    // }
    //
    // @Test
    // public void getTest() {
    // final HubNexusRestResource restResource = new HubNexusRestResource(repoReg, attHandler);
    // final HubMetaData data = restResource.get("releases", "fakepath/aura.sql/3.x/aura.sql-3.x.zip");
    //
    // Assert.assertEquals(data.getUiUrl(), "\n" + "http://int-hub02.dc1.lan:8080/api/projects/f268cc6f-5169-4e43-91a5-73ad2f3433bf/versions/05cdd7f7-43f5-46aa-81b6-dc20f936b1bf/components");
    // Assert.assertEquals(data.getPolicyOverallStatus(), "NOT_IN_VIOLATION");
    // Assert.assertEquals(data.getPolicyStatus(), "The Hub found: 0 components in violation, 0 components in violation, but overridden, and 1 components not in violation.");
    // Assert.assertEquals(data.getScanStatus(), "SUCCESS");
    // }
}
