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
 */
package org.fabric3.spi.model.physical;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * A property and its resolved values.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class PhysicalProperty {
    private String name;
    private Document value;
    private Object instanceValue;
    private boolean many;
    private QName type;

    public PhysicalProperty(String name, Document value, boolean many) {
        this(name, value, many, null);
    }

    public PhysicalProperty(String name, Document value, boolean many, QName type) {
        this.name = name;
        this.value = value;
        this.many = many;
        this.type = type;
    }

    public PhysicalProperty(String name, Object value, boolean many) {
        this.name = name;
        this.instanceValue = value;
        this.many = many;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property value. Properties may be single-valued or multi-valued. Values are stored as child nodes of the root.
     *
     * @return the property value
     */
    public Document getValue() {
        return value;
    }

    /**
     * Returns the property value as an instance.
     *
     * @return the property value
     */
    public Object getInstanceValue() {
        return instanceValue;
    }

    /**
     * Returns true if the property is multi-valued.
     *
     * @return true if the property is multi-valued
     */
    public boolean isMany() {
        return many;
    }

    /**
     * Returns the optional type.
     *
     * @return the optional type
     */
    public QName getType() {
        return type;
    }
}
