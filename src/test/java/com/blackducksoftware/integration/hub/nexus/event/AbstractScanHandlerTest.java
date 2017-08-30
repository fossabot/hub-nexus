package com.blackducksoftware.integration.hub.nexus.event;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import com.blackducksoftware.integration.hub.nexus.helpers.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.nexus.helpers.TestEventBus;
import com.blackducksoftware.integration.hub.nexus.helpers.TestingPropertyKey;
import com.blackducksoftware.integration.hub.nexus.repository.task.TaskField;

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
        lookup(ArtifactPackagingMapper.class).setPropertiesFile(propFile);
        repository.storeItem(resourceStoreRequest, new FileInputStream(zipFile), null);
        item = repository.retrieveItem(resourceStoreRequest);
        taskParameters = generateParams();
        taskParameters.put(ScanEventManager.PARAMETER_KEY_TASK_NAME, "IntegationTestTask");
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

}
