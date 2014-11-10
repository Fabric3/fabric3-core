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
package org.fabric3.api.model.type.component;

import java.net.URI;

/**
 * A promoted composite service.
 */
public class CompositeService extends ServiceDefinition {
    private static final long serialVersionUID = 7831894579780963064L;

    private URI promote;

    /**
     * Create a composite service definition.
     *
     * @param name    the name to assign to the service
     * @param promote the component service that is being promoted
     */
    public CompositeService(String name, URI promote) {
        super(name, null);
        this.promote = promote;
    }

    /**
     * Create a composite service definition.
     *
     * @param name the name to assign to the service
     */
    public CompositeService(String name) {
        super(name, null);
    }

    /**
     * Returns the URI of the component service that is being promoted.
     *
     * @return the URI of the component service that is being promoted
     */
    public URI getPromote() {
        return promote;
    }

    /**
     * Sets the URI of the component service that is being promoted.
     *
     * @param promote the URI of the component service that is being promoted
     */
    public void setPromote(URI promote) {
        this.promote = promote;
    }

    public Composite getParent() {
        return (Composite) super.getParent();
    }

    public void setParent(ComponentType parent) {
        if (!(parent instanceof Composite)) {
            throw new IllegalArgumentException("Parent must be of type " + Composite.class.getName());
        }
        super.setParent(parent);
    }

}
