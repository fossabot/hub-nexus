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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;

import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;

@Named
@Singleton
public class ScanTaskDescriptor extends AbstractHubTaskDescriptor {
    // This ID string must match the class name of the task that actually performs the operation
    public static final String ID = "ScanTask";
    public static final String PLUGIN_VERSION = "1.0.2-SNAPSHOT";
    public static final String BLACKDUCK_DIRECTORY = "blackduck";
    public static final String TASK_NAME = "Hub Repository Scan";
    public static final String DEFAULT_FILE_PATTERNS = "*.war,*.zip,*.tar.gz,*.hpi";
    public static final String DEFAULT_HUB_TIMEOUT = "300";
    public static final String DEFAULT_SCAN_MEMORY = "4096";
    public static final String DEFAULT_WORKING_DIRECTORY = "/sonatype-work";
    public static final String DEFAULT_ARTIFACT_CUTOFF = "2016-01-01T00:00:00.000";
    public static final String DEFAULT_MAX_SCANS = "100";

    private static final String DESCRIPTION_HUB_PROJECT_DISTRIBUTION = "The default distribution setting applied to the project verion if the project version is created. Possible Values: ";
    private static final String DESCRIPTION_HUB_PROJECT_PHASE = "The default phase setting applied to the project verion if the project version is created.  Possible Values: ";
    private static final String DESCRIPTION_HUB_SCAN_MEMORY = "Specify the memory, in megabytes, you would like to allocate for the BlackDuck Scan. Default: 4096";
    private static final String DESCRIPTION_REPO_NAME = "Type in the repository in which to run the task.";
    private static final String DESCRIPTION_REPO_PATH = "Enter a repository path to run the task in recursively (ie. \"/\" for root or \"/org/apache\").";
    private static final String DESCRIPTION_SCAN_FILE_PATTERN_MATCH = "The file pattern match wildcard to filter the artifacts scanned.";
    private static final String DESCRIPTION_TASK_WORKING_DIRECTORY = "The parent directory where the blackduck directory will be created to contain temporary data for the scans";
    private static final String DESCRIPTION_SCAN_CUTOFF_DATE = "If this is set, only artifacts with a modified date later than this will be scanned. To scan only artifacts newer than January 01, 2016 you would use "
            + "the cutoff format of \"2016-01-01T00:00:00.000\"";
    private static final String DESCRIPTION_RESCAN_FAILURE = "Re-scan artifacts if the previous scan result was failed";
    private static final String DESCRIPTION_ALWAYS_SCAN = "Always scan artifacts that are not too old and match the file pattern, regardless of previous scan result";
    private static final String DESCRIPTION_MAX_SCANS = "Maximum number of scans to be allowed during a single task execution.";

    private static final String LABEL_DISTRIBUTION = "Distribution";
    private static final String LABEL_FILE_PATTERN_MATCHES = "File Pattern Matches";
    private static final String LABEL_PHASE = "Phase ";
    private static final String LABEL_REPO_PATH = "Repository Path";
    private static final String LABEL_SCAN_MEMORY_ALLOCATION = "Scan Memory Allocation";
    private static final String LABEL_WORKING_DIRECTORY = "Working Directory";
    private static final String LABEL_ARTIFACT_CUTOFF = "Artifact Cutoff Date";
    private static final String LABEL_RESCAN_FAILURE = "Re-scan Failed Attempts";
    private static final String LABEL_ALWAYS_SCAN = "Always Scan Artifacts";
    private static final String LABEL_MAX_SCANS = "Max Scanned Artifacts";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField(TaskField.REPOSITORY_FIELD_ID.getParameterKey(), RepoOrGroupComboFormField.DEFAULT_LABEL, DESCRIPTION_REPO_NAME, FormField.MANDATORY);
    private final StringTextFormField resourceStorePathField = new StringTextFormField(TaskField.REPOSITORY_PATH_FIELD_ID.getParameterKey(), LABEL_REPO_PATH, DESCRIPTION_REPO_PATH, FormField.OPTIONAL);
    private final StringTextFormField artifactCutoffField = new StringTextFormField(TaskField.OLD_ARTIFACT_CUTOFF.getParameterKey(), LABEL_ARTIFACT_CUTOFF, DESCRIPTION_SCAN_CUTOFF_DATE, FormField.OPTIONAL)
            .withInitialValue(DEFAULT_ARTIFACT_CUTOFF);
    private final StringTextFormField filePatternField = new StringTextFormField(TaskField.FILE_PATTERNS.getParameterKey(), LABEL_FILE_PATTERN_MATCHES, DESCRIPTION_SCAN_FILE_PATTERN_MATCH, FormField.MANDATORY)
            .withInitialValue(DEFAULT_FILE_PATTERNS);
    private final StringTextFormField scanMemoryField = new StringTextFormField(TaskField.HUB_SCAN_MEMORY.getParameterKey(), LABEL_SCAN_MEMORY_ALLOCATION, DESCRIPTION_HUB_SCAN_MEMORY, FormField.OPTIONAL)
            .withInitialValue(DEFAULT_SCAN_MEMORY);
    private final CheckboxFormField rescanFailures = new CheckboxFormField(TaskField.RESCAN_FAILURES.getParameterKey(), LABEL_RESCAN_FAILURE, DESCRIPTION_RESCAN_FAILURE, FormField.OPTIONAL);
    private final CheckboxFormField alwaysRescan = new CheckboxFormField(TaskField.ALWAYS_SCAN.getParameterKey(), LABEL_ALWAYS_SCAN, DESCRIPTION_ALWAYS_SCAN, FormField.OPTIONAL);
    private final StringTextFormField maxScans = new StringTextFormField(TaskField.MAX_SCANS.getParameterKey(), LABEL_MAX_SCANS, DESCRIPTION_MAX_SCANS, FormField.OPTIONAL)
            .withInitialValue(DEFAULT_MAX_SCANS);
    private final ApplicationDirectories appDirectories;

    @Inject
    public ScanTaskDescriptor(final ApplicationDirectories appDirectories) {
        this.appDirectories = appDirectories;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FormField> formFields() {
        final List<FormField> fields = new ArrayList<>();

        final StringTextFormField workingDirectoryField = createWorkingDirectoryField();
        final StringTextFormField distributionFormField = createDistributionField();
        final StringTextFormField phaseFormField = createPhaseField();

        fields.add(repoField);
        fields.add(resourceStorePathField);
        fields.addAll(super.formFields());
        fields.add(distributionFormField);
        fields.add(phaseFormField);
        fields.add(scanMemoryField);
        fields.add(rescanFailures);
        fields.add(alwaysRescan);
        fields.add(maxScans);
        fields.add(filePatternField);
        fields.add(workingDirectoryField);
        fields.add(artifactCutoffField);

        return fields;
    }

    private StringTextFormField createWorkingDirectoryField() {
        StringTextFormField workingDirectoryField;
        try {
            workingDirectoryField = new StringTextFormField(TaskField.WORKING_DIRECTORY.getParameterKey(), LABEL_WORKING_DIRECTORY, DESCRIPTION_TASK_WORKING_DIRECTORY, FormField.MANDATORY)
                    .withInitialValue(appDirectories.getWorkDirectory().getCanonicalPath());
        } catch (final IOException | NullPointerException e) {
            workingDirectoryField = new StringTextFormField(TaskField.WORKING_DIRECTORY.getParameterKey(), LABEL_WORKING_DIRECTORY, DESCRIPTION_TASK_WORKING_DIRECTORY, FormField.MANDATORY).withInitialValue(DEFAULT_WORKING_DIRECTORY);
        }
        return workingDirectoryField;
    }

    private StringTextFormField createDistributionField() {
        final String possibleValues = StringUtils.join(ProjectVersionDistributionEnum.values(), ",");
        final String description = DESCRIPTION_HUB_PROJECT_DISTRIBUTION.concat(possibleValues);
        final StringTextFormField distributionFormField = new StringTextFormField(TaskField.DISTRIBUTION.getParameterKey(), LABEL_DISTRIBUTION, description, FormField.OPTIONAL)
                .withInitialValue(ProjectVersionDistributionEnum.EXTERNAL.name());
        return distributionFormField;
    }

    private StringTextFormField createPhaseField() {
        final String possibleValues = StringUtils.join(ProjectVersionPhaseEnum.values(), ",");
        final String description = DESCRIPTION_HUB_PROJECT_PHASE.concat(possibleValues);
        final StringTextFormField phaseFormField = new StringTextFormField(TaskField.PHASE.getParameterKey(), LABEL_PHASE, description, FormField.OPTIONAL).withInitialValue(ProjectVersionPhaseEnum.DEVELOPMENT.name());
        return phaseFormField;
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
