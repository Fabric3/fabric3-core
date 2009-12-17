/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ReferenceDefinition;

/**
 * Represents a reference on an instantiated component in the domain.
 *
 * @version $Rev$ $Date$
 */
public class LogicalReference extends Bindable {
    private static final long serialVersionUID = 2308698868251298609L;

    private static final QName TYPE = new QName(Constants.SCA_NS, "reference");

    private ReferenceDefinition definition;
    private List<URI> promotedUris;
    private boolean resolved;

    /**
     * Constructor.
     *
     * @param uri        the reference URI
     * @param definition the reference type definition
     * @param parent     the parent component
     */
    public LogicalReference(URI uri, ReferenceDefinition definition, LogicalComponent<?> parent) {
        super(uri, definition != null ? definition.getServiceContract() : null, parent, TYPE);
        this.definition = definition;
        promotedUris = new ArrayList<URI>();
        if (definition != null) {
            // null check for testing so full model does not need to be instantiated
            addIntents(definition.getIntents());
            addPolicySets(definition.getPolicySets());
        }
    }

    /**
     * Returns the reference type definition.
     *
     * @return the reference type definition
     */
    public ReferenceDefinition getDefinition() {
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

    @Override
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

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }


    private LogicalCompositeComponent getComposite() {
        LogicalComponent<?> parent = getParent();
        LogicalCompositeComponent composite = parent.getParent();
        return composite != null ? composite : (LogicalCompositeComponent) parent;
    }

}
