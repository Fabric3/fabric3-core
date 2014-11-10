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
package org.fabric3.spi.container.component;

import java.net.URI;
import java.util.Set;

/**
 *
 */
public class GroupInitializationException extends InstanceLifecycleException {
    private static final long serialVersionUID = 2049226987838195489L;
    private final Set<URI> componentUris;

    /**
     * Exception indicating a problem initializing a group of components.
     *
     * @param componentUris of the components that issued errors
     */
    public GroupInitializationException(Set<URI> componentUris) {
        this.componentUris = componentUris;
    }

    /**
     * Returns the URIs of components that were in error when the group was initialized.
     *
     * @return an ordered set of component uris
     */
    public Set<URI> getComponentUris() {
        return componentUris;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("Initialization errors were encountered with the following components:\n");
        for (URI uri : componentUris) {
            builder.append(uri).append("\n");
        }
        return builder.toString();
    }

    public String toString() {
        return "Error initializing components";
    }
}
