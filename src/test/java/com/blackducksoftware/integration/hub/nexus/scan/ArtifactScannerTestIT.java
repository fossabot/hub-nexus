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
package com.blackducksoftware.integration.hub.nexus.scan;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.event.HubScanEvent;
import com.blackducksoftware.integration.hub.nexus.helpers.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.helpers.TestingPropertyKey;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.util.HubEventLogger;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;
import com.blackducksoftware.integration.test.TestLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ArtifactScanner.class)
@PowerMockIgnore({ "org.apache.http.conn.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class ArtifactScannerTestIT {
    private final RestConnectionTestHelper restConnection = new RestConnectionTestHelper();
    private static final String UUID_STRING = "7dc53df5-703e-49b3-8670-b1c468f47f1f";

    @Mock
    HubEventLogger hubEventLogger;

    @Mock
    ItemAttributesHelper itemAttributesHelper;

    @Mock
    HubScanEvent hubScanEvent;

    @Mock
    StorageItem item;

    @Mock
    Repository repository;

    @Mock
    DefaultFSLocalRepositoryStorage defaultFSLocalRepositoryStorage;

    @Mock
    File repoPath;

    @Mock
    File configFile;

    @Mock
    ResourceStoreRequest resourceStoreRequest;

    @Mock
    IntegrationInfo integrationInfo;

    private Map<String, String> generateParams() {
        final Map<String, String> newParams = new HashMap<>();

        newParams.put(TaskField.HUB_URL.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL));
        newParams.put(TaskField.HUB_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_USERNAME));
        newParams.put(TaskField.HUB_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PASSWORD));
        newParams.put(TaskField.HUB_TIMEOUT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT));
        newParams.put(TaskField.HUB_PROXY_HOST.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_HOST_BASIC));
        newParams.put(TaskField.HUB_PROXY_PORT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PORT_BASIC));
        newParams.put(TaskField.HUB_PROXY_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_USER_BASIC));
        newParams.put(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PASSWORD_BASIC));
        newParams.put(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_AUTO_IMPORT_HTTPS_CERT));

        return newParams;
    }

    @Test
    public void scanTest() throws Exception {
        final Map<String, String> taskParams = new HashMap<>();
        taskParams.put(TaskField.HUB_SCAN_MEMORY.getParameterKey(), "256");
        taskParams.put(TaskField.DISTRIBUTION.getParameterKey(), "EXTERNAL");
        taskParams.put(TaskField.PHASE.getParameterKey(), "DEVELOPMENT");

        when(hubScanEvent.getItem()).thenReturn(item);
        when(hubScanEvent.getEventId()).thenReturn(UUID.fromString(UUID_STRING));
        when(hubScanEvent.getRepository()).thenReturn(repository);
        when(hubScanEvent.getTaskParameters()).thenReturn(taskParams);
        when(hubScanEvent.getRequest()).thenReturn(resourceStoreRequest);

        when(item.getPath()).thenReturn("aa-1.2.3.zip");
        when(item.getParentPath()).thenReturn("/aa/1.2.3");
        when(item.getName()).thenReturn("aa");

        when(repository.getLocalStorage()).thenReturn(defaultFSLocalRepositoryStorage);

        when(defaultFSLocalRepositoryStorage.getFileFromBase(repository, resourceStoreRequest)).thenReturn(new File(getClass().getClassLoader().getResource("repo1").getFile()));

        when(integrationInfo.getPluginVersion()).thenReturn("1");
        when(integrationInfo.getThirdPartyName()).thenReturn(ThirdPartyName.NEXUS);
        when(integrationInfo.getThirdPartyVersion()).thenReturn("2.114.4-03");

        final HubServiceHelper hubServiceHelper = new HubServiceHelper(new TestLogger(), generateParams());
        final ArtifactScanner artifactScanner = new ArtifactScanner(hubScanEvent, hubEventLogger, itemAttributesHelper, new File(getClass().getClassLoader().getResource("repo1").getFile()), hubServiceHelper, integrationInfo);
        final ProjectVersionView actual = artifactScanner.scan();

        Assert.assertEquals(actual.versionName, "1.2.3");
    }

}
