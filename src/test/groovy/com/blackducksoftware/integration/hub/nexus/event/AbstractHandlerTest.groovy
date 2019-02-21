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
package com.blackducksoftware.integration.hub.nexus.event

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField
import com.blackducksoftware.integration.hub.nexus.test.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.nexus.test.TestExecutorService
import com.blackducksoftware.integration.hub.nexus.test.TestingPropertyKey
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.service.BlackDuckService
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel
import com.synopsys.integration.log.Slf4jIntLogger
import org.apache.commons.lang3.StringUtils
import org.junit.After
import org.slf4j.LoggerFactory
import org.sonatype.nexus.AbstractMavenRepoContentTests
import org.sonatype.nexus.configuration.application.ApplicationConfiguration
import org.sonatype.nexus.proxy.ResourceStoreRequest
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler
import org.sonatype.nexus.proxy.item.RepositoryItemUid
import org.sonatype.nexus.proxy.item.StorageItem
import org.sonatype.nexus.proxy.maven.MavenHostedRepository
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.proxy.repository.Repository

public abstract class AbstractHandlerTest extends AbstractMavenRepoContentTests {
    private RestConnectionTestHelper restConnection
    private ApplicationConfiguration appConfiguration
    private DefaultAttributesHandler attributesHandler
    private ItemAttributesHelper itemAttributesHelper
    private Repository repository
    private Map<String, String> taskParameters
    private ResourceStoreRequest resourceStoreRequest
    private StorageItem item
    private ProjectRequest projectRequest
    private HubServiceHelper hubServiceHelper
    private TestExecutorService testExecutorService

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        setupTest(getZipFilePath())
    }

    @After
    public void cleanUpAfterTest() throws Exception {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()))
        final BlackDuckServicesFactory hubServicesFactory = restConnection.createHubServicesFactory(intLogger)
        final ProjectService projectRequestService = hubServicesFactory.createProjectService()
        BlackDuckService blackDuckService = hubServicesFactory.createBlackDuckService();
        final Optional<ProjectView> projectView = projectRequestService.getProjectByName(projectRequest.getName())
        if (projectView.isPresent()) {
            ProjectView project = projectView.get()
            blackDuckService.delete(project)
        }
    }


    public abstract String getZipFilePath()

    private void setupTest(final String zipFilePath) throws Exception {
        testExecutorService = new TestExecutorService()
        appConfiguration = this.nexusConfiguration()
        attributesHandler = lookup(DefaultAttributesHandler.class)
        itemAttributesHelper = new ItemAttributesHelper(attributesHandler);
        restConnection = new RestConnectionTestHelper()
        final File zipFile = getTestFile(zipFilePath)
        final File propFile = getTestFile("src/test/resources/repo1/extension-mapping.properties")
        resourceStoreRequest = new ResourceStoreRequest("/integration/test/0.0.1-SNAPSHOT/" + zipFile.getName())
        repository = lookup(RepositoryRegistry.class).getRepositoryWithFacet("snapshots", MavenHostedRepository.class)
        if (StringUtils.isBlank(resourceStoreRequest.getRequestPath())) {
            resourceStoreRequest.setRequestPath(RepositoryItemUid.PATH_ROOT)
        }
        resourceStoreRequest.setRequestLocalOnly(true)
        lookup(ArtifactPackagingMapper.class).setPropertiesFile(propFile)
        final FileInputStream zipFileInputStream = new FileInputStream(zipFile)
        repository.storeItem(resourceStoreRequest, zipFileInputStream, null)
        item = repository.retrieveItem(resourceStoreRequest)
        taskParameters = generateParams()
        projectRequest = createProjectRequest()
        resourceStoreRequest = new ResourceStoreRequest("")
        hubServiceHelper = new HubServiceHelper(new Slf4jIntLogger(LoggerFactory.getLogger(getClass())), taskParameters);
    }

    private ProjectRequest createProjectRequest() {
        ProjectSyncModel projectSyncModel = new ProjectSyncModel();
        projectSyncModel.setName(restConnection.getProperty(TestingPropertyKey.TEST_PROJECT))
        projectSyncModel.setVersionName(restConnection.getProperty(TestingPropertyKey.TEST_VERSION))
        projectSyncModel.setProjectLevelAdjustments(true)
        projectSyncModel.setPhase(restConnection.getProperty(TestingPropertyKey.TEST_PHASE))
        projectSyncModel.setDistribution(restConnection.getProperty(TestingPropertyKey.TEST_DISTRIBUTION))
        return projectSyncModel.createProjectRequest()
    }

    protected HubScanEvent processItem(final ScanItemMetaData data) {
        final HubScanEvent event = new HubScanEvent(data.getItem().getRepositoryItemUid().getRepository(), data.getItem(), data.getTaskParameters(), data.getRequest(), data.getProjectRequest());
        return event;
    }

    @Override
    protected boolean runWithSecurityDisabled() {
        return true
    }

    private Map<String, String> generateParams() {
        final Map<String, String> newParams = new HashMap<>()
        newParams.put(TaskField.HUB_URL.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL))
        newParams.put(TaskField.HUB_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_USERNAME))
        newParams.put(TaskField.HUB_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PASSWORD))
        newParams.put(TaskField.HUB_TIMEOUT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT))
        newParams.put(TaskField.HUB_PROXY_HOST.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_HOST_BASIC))
        newParams.put(TaskField.HUB_PROXY_PORT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PORT_BASIC))
        newParams.put(TaskField.HUB_PROXY_USERNAME.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_USER_BASIC))
        newParams.put(TaskField.HUB_PROXY_PASSWORD.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_PROXY_PASSWORD_BASIC))
        newParams.put(TaskField.HUB_AUTO_IMPORT_CERT.getParameterKey(), restConnection.getProperty(TestingPropertyKey.TEST_AUTO_IMPORT_HTTPS_CERT))

        return newParams
    }

    public RestConnectionTestHelper getRestConnection() {
        return restConnection
    }

    public ApplicationConfiguration getAppConfiguration() {
        return appConfiguration
    }

    public DefaultAttributesHandler getAttributesHandler() {
        return attributesHandler
    }

    public ItemAttributesHelper getItemAttributesHelper() {
        return itemAttributesHelper
    }

    public HubServiceHelper getHubServiceHelper() {
        return hubServiceHelper
    }

    public Repository getRepository() {
        return repository
    }

    public Map<String, String> getTaskParameters() {
        return taskParameters
    }

    public ResourceStoreRequest getResourceStoreRequest() {
        return resourceStoreRequest
    }

    public StorageItem getItem() {
        return item
    }

    public ProjectRequest getProjectRequest() {
        return projectRequest
    }
}
