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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.sonatype.nexus.plugin.PluginIdentity;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;

import com.blackducksoftware.integration.hub.nexus.application.HubServiceHelper;
import com.blackducksoftware.integration.hub.nexus.application.IntegrationInfo;
import com.blackducksoftware.integration.hub.nexus.repository.task.walker.TaskWalker;
import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;
import com.blackducksoftware.integration.hub.nexus.util.ParallelEventProcessor;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeResponse;

public abstract class AbstractHubWalkerTask extends AbstractNexusRepositoriesPathAwareTask<Object> {
    protected static final String ALL_REPO_ID = "all_repo";
    protected ItemAttributesHelper itemAttributesHelper;
    protected TaskWalker taskWalker;
    protected ParallelEventProcessor parallelEventProcessor;
    private HubServiceHelper hubServiceHelper;
    private final IntegrationInfo integrationInfo;

    public AbstractHubWalkerTask(final TaskWalker taskWalker, final DefaultAttributesHandler attributesHandler, final IntegrationInfo integrationInfo, final ParallelEventProcessor parallelEventProcessor) {
        this.taskWalker = taskWalker;
        itemAttributesHelper = new ItemAttributesHelper(attributesHandler);
        this.integrationInfo = integrationInfo;
        this.parallelEventProcessor = parallelEventProcessor;
    }

    protected HubServiceHelper getHubServiceHelper() {
        if (hubServiceHelper == null) {
            hubServiceHelper = new HubServiceHelper(new Slf4jIntLogger(logger), getParameters());
        }

        return hubServiceHelper;
    }

    protected List<Repository> createRepositoryList() {
        final List<Repository> repositoryList = new ArrayList<>();
        final String repositoryFieldId = getParameter(TaskField.REPOSITORY_FIELD_ID.getParameterKey());
        if (StringUtils.isNotBlank(repositoryFieldId) && !ALL_REPO_ID.equals(repositoryFieldId)) {
            try {
                repositoryList.add(getRepositoryRegistry().getRepository(repositoryFieldId));
            } catch (final NoSuchRepositoryException e) {
                logger.warn("No repositories found to walk");
            }
        } else {
            for (final Repository repository : getRepositoryRegistry().getRepositories()) {
                if (!repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
                    repositoryList.add(repository);
                }
            }
        }

        return repositoryList;
    }

    @Override
    protected final Object doRun() throws Exception {
        Optional<PhoneHomeResponse> phoneHomeResponse = Optional.empty();
        try {
            initTask();
            phoneHomeResponse = phoneHomeIfApplicable();
            final AbstractWalkerProcessor repositoryWalker = getRepositoryWalker();
            final DefaultStoreWalkerFilter repositoryWalkerFilter = getRepositoryWalkerFilter();

            final List<Repository> repositoryList = createRepositoryList();
            taskWalker.walkRepositoriesWithFilter(repositoryList, repositoryWalker, repositoryWalkerFilter, getResourceStorePath());
        } catch (final Exception ex) {
            logger.error("Error occurred during task execution {}", ex);
        } finally {
            parallelEventProcessor.shutdownProcessor();
            phoneHomeResponse.ifPresent(this::endPhoneHome);
        }
        return null;
    }

    private Optional<PhoneHomeResponse> phoneHomeIfApplicable() {
        final DateTime currentTime = new DateTime();
        if (shouldPhoneHome(currentTime)) {
            logger.info("Sending phone home data");
            final Optional<PhoneHomeResponse> response = phoneHome();
            addParameter(TaskField.PHONE_HOME.getParameterKey(), currentTime.toString());
            return response;
        }
        return Optional.empty();
    }

    public Optional<PhoneHomeResponse> phoneHome() {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final BlackDuckPhoneHomeHelper phoneHomeHelper = BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(getHubServiceHelper().getHubServicesFactory(), executorService);
            logger.debug("Sending phone home data.");

            final Map<String, String> metaData = new HashMap();
            metaData.put("nexus.version", integrationInfo.getThirdPartyVersion());

            final PluginIdentity pluginIdentity = integrationInfo.getPluginIdentity();

            logger.debug("Found {} version {}", pluginIdentity.getId(), pluginIdentity.getVersion());

            return Optional.of(phoneHomeHelper.handlePhoneHome(pluginIdentity.getId(), pluginIdentity.getVersion(), metaData));
        } catch (final IllegalArgumentException e) {
            logger.debug("Problem with phoning home", e);
        } finally {
            executorService.shutdownNow();
        }
        return Optional.empty();
    }

    public void endPhoneHome(final PhoneHomeResponse phoneHomeResponse) {
        if (phoneHomeResponse.getImmediateResult()) {
            logger.debug("Phone home was successful.");
        } else {
            logger.debug("Phone home failed.");
        }
    }

    private boolean shouldPhoneHome(final DateTime currentTime) {
        DateTime cachedDate = new DateTime(0L);
        final String cachedDateString = getParameter(TaskField.PHONE_HOME.getParameterKey());
        if (StringUtils.isNoneBlank(cachedDateString)) {
            cachedDate = DateTime.parse(cachedDateString);
        }

        return currentTime.minusDays(1).isAfter(cachedDate);
    }

    protected void initTask() throws Exception {
        // Override if needed
    }

    protected abstract AbstractWalkerProcessor getRepositoryWalker();

    protected abstract DefaultStoreWalkerFilter getRepositoryWalkerFilter();

}
