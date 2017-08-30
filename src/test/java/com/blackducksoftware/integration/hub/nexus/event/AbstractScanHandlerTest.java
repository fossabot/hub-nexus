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
package com.blackducksoftware.integration.hub.nexus.event;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.test.TestEventBus;
import com.blackducksoftware.integration.hub.nexus.test.TestingPropertyKey;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;

public class AbstractScanHandlerTest extends AbstractMavenRepoContentTests {

    private RestConnectionTestHelper restConnection;
    private ApplicationConfiguration appConfiguration;
    private TestEventBus eventBus;
    private DefaultAttributesHandler attributesHandler;
    private ScanEventManager eventManager;
    private Repository repository;
    private Map<String, String> taskParameters;
    private ResourceStoreRequest resourceStoreRequest;
    private StorageItem item;
    private ProjectRequest projectRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new TestEventBus();
        appConfiguration = this.nexusConfiguration();
        attributesHandler = lookup(DefaultAttributesHandler.class);
        eventManager = new ScanEventManager(eventBus);
        restConnection = new RestConnectionTestHelper();
        final File zipFile = getTestFile("src/test/resources/repo1/aa-1.2.3.zip");
        final File propFile = getTestFile("src/test/resources/repo1/packaging2extension-mapping.properties");
        resourceStoreRequest = new ResourceStoreRequest("/integration/test/1.0-SNAPSHOT/" + zipFile.getName());
        repository = lookup(RepositoryRegistry.class).getRepositoryWithFacet("snapshots", MavenHostedRepository.class);
        if (StringUtils.isBlank(resourceStoreRequest.getRequestPath())) {
            resourceStoreRequest.setRequestPath(RepositoryItemUid.PATH_ROOT);
        }
        resourceStoreRequest.setRequestLocalOnly(true);
        lookup(ArtifactPackagingMapper.class).setPropertiesFile(propFile);
        repository.storeItem(resourceStoreRequest, new FileInputStream(zipFile), null);
        item = repository.retrieveItem(resourceStoreRequest);
        taskParameters = generateParams();
        taskParameters.put(ScanEventManager.PARAMETER_KEY_TASK_NAME, "IntegationTestTask");
        projectRequest = createProjectRequest();
        resourceStoreRequest = new ResourceStoreRequest("");
    }

    private ProjectRequest createProjectRequest() {
        final ProjectRequestBuilder builder = new ProjectRequestBuilder();
        builder.setProjectName("hub-nexus-it");
        builder.setVersionName("0.0.1-SNAPSHOT");
        builder.setProjectLevelAdjustments(true);
        builder.setPhase(ProjectVersionPhaseEnum.DEVELOPMENT);
        builder.setDistribution(ProjectVersionDistributionEnum.INTERNAL);
        return builder.build();
    }

    @Override
    protected boolean runWithSecurityDisabled() {
        return true;
    }

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

    public RestConnectionTestHelper getRestConnection() {
        return restConnection;
    }

    public ApplicationConfiguration getAppConfiguration() {
        return appConfiguration;
    }

    public TestEventBus getEventBus() {
        return eventBus;
    }

    public DefaultAttributesHandler getAttributesHandler() {
        return attributesHandler;
    }

    public ScanEventManager getEventManager() {
        return eventManager;
    }

    public Repository getRepository() {
        return repository;
    }

    public Map<String, String> getTaskParameters() {
        return taskParameters;
    }

    public ResourceStoreRequest getResourceStoreRequest() {
        return resourceStoreRequest;
    }

    public StorageItem getItem() {
        return item;
    }

    public ProjectRequest getProjectRequest() {
        return projectRequest;
    }
}
