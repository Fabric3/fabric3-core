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
package org.fabric3.spi.model.instance;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * Holds a parsed component property as a DOM.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class LogicalProperty extends LogicalScaArtifact<LogicalComponent<?>> {
    private static final long serialVersionUID = 4648573312983221666L;

    private String name;
    private Document value;
    private Object instanceValue;
    private boolean many;
    private QName type;

    public LogicalProperty(String name, Document value, boolean many, LogicalComponent<?> parent) {
        super(parent);
        this.name = name;
        this.value = value;
        this.many = many;
    }

    public LogicalProperty(String name, Document value, boolean many, QName type, LogicalComponent<?> parent) {
        super(parent);
        this.name = name;
        this.value = value;
        this.many = many;
        this.type = type;
    }

    public LogicalProperty(String name, Object instanceValue, LogicalComponent<?> parent) {
        super(parent);
        this.name = name;
        this.instanceValue = instanceValue;
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
     * The parsed property value.
     *
     * @return the value
     */
    public Document getValue() {
        return value;
    }

    /**
     * Returns the set instance value.
     *
     * @return the set instance value
     */
    public Object getInstanceValue() {
        return instanceValue;
    }

    /**
     * True if this property is many-valued.
     *
     * @return true if this property is many-valued
     */
    public boolean isMany() {
        return many;
    }

    /**
     * Returns the XSD type or null of the property.
     *
     * @return the XSD type or null of the property.
     */
    public QName getType() {
        return type;
    }
}

