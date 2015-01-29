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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.Autowire;
import org.fabric3.api.model.type.component.ComponentReference;

/**
 * A reference on an instantiated component in the domain.
 */
public class LogicalReference extends Bindable {
    private static final long serialVersionUID = 2308698868251298609L;

    private AbstractReference definition;
    private List<URI> promotedUris;
    private boolean resolved;
    private Autowire autowire = Autowire.INHERITED;
    private LogicalReference leafReference;

    /**
     * Constructor.
     *
     * @param uri        the reference URI
     * @param definition the reference type definition
     * @param parent     the parent component
     */
    public LogicalReference(URI uri, AbstractReference<?> definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
        promotedUris = new ArrayList<>();
        leafReference = this;
    }

    /**
     * Returns the reference type definition.
     *
     * @return the reference type definition
     */
    public AbstractReference getDefinition() {
        return definition;
    }

    /**
     * Returns the wires for the reference.
     *
     * @return the wires for the reference
     */
    public List<LogicalWire> getWires() {
        return getComposite().getWires(this);
    }

    /**
     * Returns the URIs of component references promoted by this reference.
     *
     * @return the URIs
     */
    public List<URI> getPromotedUris() {
        return promotedUris;
    }

    /**
     * Adds the URI of a component reference promoted by this reference.
     *
     * @param uri the promoted URI
     */
    public void addPromotedUri(URI uri) {
        promotedUris.add(uri);
    }

    /**
     * Sets the  URI of the reference promoted by this reference at the given index
     *
     * @param index the index
     * @param uri   the  URI
     */
    public void setPromotedUri(int index, URI uri) {
        promotedUris.set(index, uri);
    }

    public Autowire getAutowire() {
        return autowire;
    }

    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns true if this reference's target (or targets) has been resolved.
     *
     * @return true if this reference's target (or targets) has been resolved
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Sets if this reference's target (or targets) has been resolved.
     *
     * @param resolved true if resolved.
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * Gets the explicit reference associated with this logical reference.
     *
     * @return Component reference if defined, otherwise null.
     */
    public ComponentReference getComponentReference() {
        return getParent().getDefinition().getReferences().get(getDefinition().getName());
    }

    public LogicalReference getLeafReference() {
        return leafReference;
    }

    public void setLeafReference(LogicalReference leafReference) {
        this.leafReference = leafReference;
    }

    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalReference test = (LogicalReference) obj;
        return getUri().equals(test.getUri());

    }

    public int hashCode() {
        return getUri().hashCode();
    }


    private LogicalCompositeComponent getComposite() {
        LogicalComponent<?> parent = getParent();
        LogicalCompositeComponent composite = parent.getParent();
        return composite != null ? composite : (LogicalCompositeComponent) parent;
    }

}
