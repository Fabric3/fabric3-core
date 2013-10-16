/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.model.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The base class for assembly model types.
 */
public abstract class ModelObject<P extends ModelObject> implements Serializable {
    private static final long serialVersionUID = -4731760911483352681L;

    protected boolean roundTrip;
    private List<ModelObject> elementStack;
    private Set<String> attributes;
    private P parent;

    /**
     * Adds a text value read from the composite serialized form.
     *
     * @param val a text value read from the composite serialized form
     */
    public void addText(String val) {
        if (!roundTrip) {
            return;
        }
        Text text = new Text(val);
        text.setParent(this);
        pushElement(text);
    }

    /**
     * Adds a Comment read from the composite serialized form.
     *
     * @param val a Comment read from the composite serialized form
     */
    public void addComment(String val) {
        if (!roundTrip) {
            return;
        }
        Comment comment = new Comment(val);
        comment.setParent(this);
        pushElement(comment);
    }

    /**
     * Turns on round trip support from serialized to in-memory form for the type.
     */
    public void enableRoundTrip() {
        roundTrip = true;
    }

    /**
     * Returns the attributes specified on the type element when it was deserialized.
     *
     * @return the attributes specified on the type element when it was deserialized
     */
    public Set<String> getSpecifiedAttributes() {
        if (attributes == null) {
            return Collections.emptySet();
        }
        return attributes;
    }

    /**
     * Records the position of an attribute as it is read from serialized form.
     *
     * @param attribute the element being read
     */
    public void attributeSpecified(String attribute) {
        if (!roundTrip) {
            return;
        }
        if (attributes == null) {
            attributes = new HashSet<String>();
        }
        attributes.add(attribute);
    }

    /**
     * Returns the elements of the type in the order they were read in serialized form.
     *
     * @return the elements of the type in the order they were read in serialized form
     */
    public List<ModelObject> getElementStack() {
        if (elementStack == null) {
            return Collections.emptyList();
        }
        return elementStack;
    }

    /**
     * Records the position of an element as it is read from serialized form.
     *
     * @param element the element being read
     */
    protected void pushElement(ModelObject element) {
        if (!roundTrip) {
            return;
        }
        if (elementStack == null) {
            elementStack = new ArrayList<ModelObject>();
        }
        elementStack.add(element);
    }

    /**
     * Removes an element from the stack.
     *
     * @param element the element to remove
     */
    protected void removeElement(ModelObject element) {
        if (!roundTrip || elementStack == null) {
            return;
        }
        elementStack.remove(element);
    }

    /**
     * Returns the parent of this object or null if there is no parent.
     *
     * @return the parent of this object or nul
     */
    public P getParent() {
        return parent;
    }

    /**
     * Sets the parent of this object or null if there is no parent.
     *
     * @param parent the parent of this object or nul
     */
    public void setParent(P parent) {
        this.parent = parent;
    }
}
