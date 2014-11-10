/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.contribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.failure.ValidationFailure;

/**
 *
 */
public class ArtifactValidationFailure extends ValidationFailure {
    private List<ValidationFailure> failures;
    private URI contributionUri;
    private String artifactName;

    public ArtifactValidationFailure(URI contributionUri, String artifactName) {
        this.contributionUri = contributionUri;
        this.artifactName = artifactName;
        this.failures = new ArrayList<>();
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public List<ValidationFailure> getFailures() {
        return failures;
    }

    public void addFailure(ValidationFailure failure) {
        failures.add(failure);
    }

    public void addFailures(List<ValidationFailure> failures) {
        this.failures.addAll(failures);
    }

    public String getMessage() {
        return "Errors were reported in " + artifactName + " in contribution " + contributionUri;
    }

}
