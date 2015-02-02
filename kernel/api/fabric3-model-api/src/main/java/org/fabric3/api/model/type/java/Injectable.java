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
package org.fabric3.api.model.type.java;

import org.fabric3.api.model.type.ModelObject;

/**
 * Identifies an attribute of the component that can be injected into an instance.
 */
public class Injectable extends ModelObject<InjectingComponentType> {
    public static final Injectable OASIS_COMPONENT_CONTEXT = new Injectable(InjectableType.CONTEXT, "OASISComponentContext");
    public static final Injectable OASIS_REQUEST_CONTEXT = new Injectable(InjectableType.CONTEXT, "RequestContext");

    private InjectableType type;

    private String name;

    /**
     * Constructor used for deserialization.
     */
    public Injectable() {
    }

    /**
     * Constructor specifying type of value and logical name.
     *
     * @param type the type of value
     * @param name the logical name
     */
    public Injectable(InjectableType type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Returns the type (service, reference, property).
     *
     * @return the type of value this source represents
     */
    public InjectableType getType() {
        return type;
    }

    /**
     * Sets the type (callback, reference, property).
     *
     * @param type the type of value this source represents
     */
    public void setType(InjectableType type) {
        this.type = type;
    }

    /**
     * Returns the name.
     *
     * @return the name of this value
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this value.
     *
     * @param name the name of this value
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name + '[' + type + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Injectable that = (Injectable) o;
        return name.equals(that.name) && type == that.type;

    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + name.hashCode();
    }
}
