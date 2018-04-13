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
package com.blackducksoftware.integration.hub.nexus.repository.task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;

@Named
@Singleton
public class PolicyCheckTaskDescriptor extends AbstractHubTaskDescriptor {
    public static final String ID = "PolicyCheckTask";
    public static final String TASK_NAME = "Hub Policy Check";

    public static final String DEFAULT_MAX_PARALLEL_POLICY_CHECKS = "100";

    public static final String LABEL_MAX_PARALLEL_POLICY_CHECKS = "Max Parallel Policy checks";

    public static final String DESCRIPTION_MAX_PARALLEL_POLICY_CHECKS = "Maximum number of policy checks to make at the same time. Decrease this value to increase performance. 0 or blank will perform this task synchronously.";

    private final StringTextFormField policyCheckParallel = new StringTextFormField(TaskField.MAX_PARALLEL_SCANS.getParameterKey(), LABEL_MAX_PARALLEL_POLICY_CHECKS, DESCRIPTION_MAX_PARALLEL_POLICY_CHECKS, FormField.OPTIONAL)
            .withInitialValue(DEFAULT_MAX_PARALLEL_POLICY_CHECKS);

    @SuppressWarnings("rawtypes")
    @Override
    public List<FormField> formFields() {
        final List<FormField> fields = new ArrayList<>();

        fields.addAll(super.formFields());
        fields.add(policyCheckParallel);

        return fields;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }
}
