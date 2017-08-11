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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.sonatype.sisu.goodies.common.Loggers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias(value = "blackduck-info")
@XmlRootElement(name = "blackduck-info")
// @XmlAccessorType(XmlAccessType.FIELD)
public class MetaDataResponse {
    final Logger logger = Loggers.getLogger(MetaDataResponse.class);

    private HubMetaData data;

    public MetaDataResponse() {
        logger.info("MetaDataResponse");
    }

    @XmlElementWrapper(name = "data")
    @XmlElement(name = "hubMeta")
    public HubMetaData getData() {
        return data;
    }

    public void setData(final HubMetaData data) {
        this.data = data;
    }
}
