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
package org.fabric3.spi.model.type.java;

import java.lang.reflect.Field;

import org.fabric3.api.model.type.java.InjectionSite;

/**
 * Represents a field that is injected into when a component implementation instance is instantiated.
 */
public class FieldInjectionSite extends InjectionSite {
    private String name;
    private Field field;

    public FieldInjectionSite(Field field) {
        super(field.getType());
        this.name = field.getName();
        this.field = field;
    }

    /**
     * Gets the name of the field.
     *
     * @return Site name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the field
     *
     * @return the field
     */
    public Field getField() {
        return field;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldInjectionSite that = (FieldInjectionSite) o;
        return name.equals(that.name);

    }

    public int hashCode() {
        return name.hashCode();
    }

}
