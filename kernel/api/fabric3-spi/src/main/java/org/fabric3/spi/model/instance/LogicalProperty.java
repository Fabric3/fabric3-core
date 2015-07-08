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
 * Holds a parsed component property.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class LogicalProperty extends LogicalScaArtifact<LogicalComponent<?>> {
    private static final long serialVersionUID = 4648573312983221666L;

    private String name;
    private Document xmlValue;
    private Object instanceValue;
    private String key;
    private boolean many;
    private QName type;
    private boolean required = true;

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
    public Document getXmlValue() {
        return xmlValue;
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
     * Returns the key if the property is sourced externally.
     *
     * @return the key or null
     */
    public String getKey() {
        return key;
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

    /**
     * Returns true if the property is required.
     *
     * @return true if the property is required
     */
    public boolean isRequired() {
        return required;
    }

    private LogicalProperty(String name, LogicalComponent<?> parent) {
        super(parent);
        this.name = name;
    }

    public static class Builder {
        private LogicalProperty property;

        public static Builder newBuilder(String name, LogicalComponent<?> parent) {
            return new Builder(name, parent);
        }

        public Builder xmlValue(Document value) {
            property.xmlValue = value;
            return this;
        }

        public Builder type(QName type) {
            property.type = type;
            return this;
        }

        public Builder many(boolean value) {
            property.many = value;
            return this;
        }

        public Builder required(boolean value) {
            property.required = value;
            return this;
        }

        public Builder instanceValue(Object value) {
            property.instanceValue = value;
            return this;
        }

        public Builder key(String key) {
            property.key = key;
            return this;
        }

        public LogicalProperty build() {
            return property;
        }

        private Builder(String name, LogicalComponent<?> parent) {
            property = new LogicalProperty(name, parent);
            property.name = name;
        }

    }
}

