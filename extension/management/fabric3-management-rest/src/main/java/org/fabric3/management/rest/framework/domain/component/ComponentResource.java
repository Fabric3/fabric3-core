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
package org.fabric3.management.rest.framework.domain.component;

import java.net.URI;

import org.fabric3.management.rest.model.Resource;

/**
 * A deployed component.
 */
public class ComponentResource extends Resource {
    private static final long serialVersionUID = -1231963830775425265L;
    private URI uri;
    private String zone;

    /**
     * Constructor.
     *
     * @param uri  the component URI
     * @param zone the zone the component is deployed to
     */
    public ComponentResource(URI uri, String zone) {
        this.uri = uri;
        this.zone = zone;
    }

    /**
     * Returns the component URI.
     *
     * @return the component URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Return the zone the component is deployed to.
     *
     * @return the zone the component is deployed to
     */
    public String getZone() {
        return zone;
    }
}
