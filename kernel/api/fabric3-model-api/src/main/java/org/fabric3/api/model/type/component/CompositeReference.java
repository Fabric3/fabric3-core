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
import java.util.ArrayList;
import java.util.List;

/**
 * A promoted composite reference.
 */
public class CompositeReference extends ReferenceDefinition {
    private static final long serialVersionUID = 5387987439912912994L;

    private List<URI> promotedUris;

    /**
     * Construct a composite reference.
     *
     * @param name         the name of the composite reference
     * @param promotedUris the list of component references it promotes
     * @param multiplicity the reference multiplicity
     */
    public CompositeReference(String name, List<URI> promotedUris, Multiplicity multiplicity) {
        super(name, null, multiplicity);
        if (promotedUris != null) {
            this.promotedUris = promotedUris;
        } else {
            this.promotedUris = new ArrayList<>();
        }
    }

    /**
     * Construct a composite reference.
     *
     * @param name the name of the composite reference
     */
    public CompositeReference(String name) {
        super(name, null, Multiplicity.ONE_ONE);
        this.promotedUris = new ArrayList<>();
    }


    /**
     * Returns the list of references this composite reference promotes.
     *
     * @return the list of references this composite reference promotes
     */
    public List<URI> getPromotedUris() {
        return promotedUris;
    }

    /**
     * Sets the list of references this composite reference promotes.
     *
     * @param promotedUris the list of references this composite reference promotes
     */
    public void setPromotedUris(List<URI> promotedUris) {
        this.promotedUris = promotedUris;
    }

    /**
     * Adds the URI of a reference this composite reference promotes.
     *
     * @param uri the promoted reference URI
     */
    public void addPromotedUri(URI uri) {
        promotedUris.add(uri);
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
