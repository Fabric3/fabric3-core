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
import java.util.List;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ReferenceDefinition;

/**
 * A reference on an instantiated component in the domain.
 */
public class LogicalReference extends LogicalBindable {
    private static final long serialVersionUID = 2308698868251298609L;

    private ReferenceDefinition<ComponentType> definition;
    private boolean resolved;
    private LogicalReference leafReference;

    /**
     * Constructor.
     *
     * @param uri        the reference URI
     * @param definition the reference type definition
     * @param parent     the parent component
     */
    public LogicalReference(URI uri, ReferenceDefinition<ComponentType> definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent);
        this.definition = definition;
        leafReference = this;
    }

    /**
     * Returns the reference type definition.
     *
     * @return the reference type definition
     */
    public ReferenceDefinition<ComponentType> getDefinition() {
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
    public ReferenceDefinition<ComponentDefinition> getComponentReference() {
        return getParent().getDefinition().getReferences().get(getDefinition().getName());
    }

    public LogicalReference getLeafReference() {
        return leafReference;
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
