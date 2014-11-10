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
 */
package org.fabric3.management.rest.framework.domain.contribution;

import java.net.URI;

import org.fabric3.management.rest.model.Link;

/**
 * Provides information on a contribution installed in the domain.
 */
public class ContributionStatus {
    private URI uri;
    private String state;
    private Link link;

    /**
     * Constructor.
     *
     * @param uri   the contribution URI
     * @param state the contribution state
     * @param link  the contribution resource URL
     */
    public ContributionStatus(URI uri, String state, Link link) {
        this.uri = uri;
        this.state = state;
        this.link = link;
    }

    /**
     * Returns the contribution URI.
     *
     * @return the contribution URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the contribution state.
     *
     * @return the contribution state
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the contribution resource URL.
     *
     * @return the contribution resource URL
     */
    public Link getLink() {
        return link;
    }
}
