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
import java.util.ArrayList;
import java.util.List;

/**
 * A deployed composite.
 */
public class CompositeResource extends ComponentResource {
    private static final long serialVersionUID = -2376688479953947981L;
    private List<ComponentResource> components = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param uri  the composite URI
     * @param zone the zone the composite is deployed to
     */
    public CompositeResource(URI uri, String zone) {
        super(uri, zone);
    }

    public void addComponent(ComponentResource resource) {
        components.add(resource);
    }

    public List<ComponentResource> getComponents() {
        return components;
    }
}
