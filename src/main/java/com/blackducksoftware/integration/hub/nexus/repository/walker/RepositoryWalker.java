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
package com.blackducksoftware.integration.hub.nexus.repository.walker;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class RepositoryWalker extends AbstractWalkerProcessor {

    private final HubServerConfig hubServerConfig;

    private final HubServicesFactory hubServicesFactory;

    private final Logger logger = Loggers.getLogger(getClass());

    private final String fileMatcherString = "*.war,*.zip,*.tar.gz,*.hpi";

    public RepositoryWalker(final HubServerConfig hubServerConfig, final HubServicesFactory hubServicesFactory) {
        this.hubServerConfig = hubServerConfig;
        this.hubServicesFactory = hubServicesFactory;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
        try {
            if (item instanceof StorageCollectionItem) {
                return; // directory found
            }

            if (item.getRepositoryItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)) {
                return;
            }
            final String[] patternArray = StringUtils.split(fileMatcherString, ",");
            for (final String wildCardPattern : patternArray) {
                if (FilenameUtils.wildcardMatch(item.getPath(), wildCardPattern)) {
                    logger.info("Path of " + item.getName() + ": " + item.getPath());
                    final ArtifactScanner scanner = new ArtifactScanner(hubServerConfig, hubServicesFactory, context.getRepository(),
                            context.getResourceStoreRequest(), item);
                    scanner.scan();
                    break;
                }
            }
        } catch (final Exception ex) {
            logger.error(String.format("Error occurred in walker processor for repository: %s", ex));
        }
    }
}
