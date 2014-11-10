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
package org.fabric3.api.host.contribution;

import java.net.URI;
import java.util.Set;

/**
 * Thrown when there is an attempt to unload a contribution referenced by other installed contributions.
 */
public class ContributionInUseException extends UninstallException {
    private static final long serialVersionUID = 3826037592455762508L;
    private Set<URI> contributions;
    private URI uri;

    /**
     * Constructor.
     *
     * @param message       the error message
     * @param uri           the contribution
     * @param contributions the installed contributions that reference the contribution
     */
    public ContributionInUseException(String message, URI uri, Set<URI> contributions) {
        super(message);
        this.uri = uri;
        this.contributions = contributions;
    }

    public Set<URI> getContributions() {
        return contributions;
    }

    public URI getUri() {
        return uri;
    }
}
