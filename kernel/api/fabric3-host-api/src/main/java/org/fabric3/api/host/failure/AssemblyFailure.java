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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.failure;

import java.net.URI;
import java.util.List;

/**
 * Base class for recoverable errors updating the domain assembly encountered during a deployment.
 */
public abstract class AssemblyFailure extends Failure {
    private URI componentUri;
    private URI contributionUri;
    private List<?> sources;

    /**
     * Constructor.
     *
     * @param componentUri    the URI of the component associated with the failure.
     * @param contributionUri the URI of the contribution the component is part of.
     * @param sources         the error sources
     */
    public AssemblyFailure(URI componentUri, URI contributionUri, List<?> sources) {
        this.componentUri = componentUri;
        this.contributionUri = contributionUri;
        this.sources = sources;
    }

    /**
     * Returns the URI of the contribution where the error occurred.
     *
     * @return the URI of the contribution where the error occurred.
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Returns the URI of the component where the error occurred.
     *
     * @return the URI of the component where the error occurred.
     */
    public URI getComponentUri() {
        return componentUri;
    }

    public List<?> getSources() {
        return sources;
    }
}
