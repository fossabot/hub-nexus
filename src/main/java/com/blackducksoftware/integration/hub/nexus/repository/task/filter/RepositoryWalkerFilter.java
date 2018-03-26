/*
 * hub-nexus
 *
 * 	Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.nexus.repository.task.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.sisu.goodies.common.Loggers;

import com.blackducksoftware.integration.hub.nexus.util.ItemAttributesHelper;

public abstract class RepositoryWalkerFilter extends DefaultStoreWalkerFilter {
    protected final Logger logger = Loggers.getLogger(getClass());
    protected final ItemAttributesHelper itemAttributesHelper;

    public RepositoryWalkerFilter(final ItemAttributesHelper itemAttributesHelper) {
        this.itemAttributesHelper = itemAttributesHelper;
    }

    @Override
    public boolean shouldProcess(final WalkerContext context, final StorageItem item) {
        if (item instanceof StorageCollectionItem) {
            return false; // directory found
        }
        if (item.getRepositoryItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)) {
            return false;
        }

        if (StringUtils.isNotBlank(item.getRemoteUrl())) {
            logger.info("Item came from a proxied repository, skipping: {}", item);
            return false;
        }

        return true;
    }
}
