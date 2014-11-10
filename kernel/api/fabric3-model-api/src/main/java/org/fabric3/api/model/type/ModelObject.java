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
package org.fabric3.api.model.type;

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
            attributes = new HashSet<>();
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
            elementStack = new ArrayList<>();
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
