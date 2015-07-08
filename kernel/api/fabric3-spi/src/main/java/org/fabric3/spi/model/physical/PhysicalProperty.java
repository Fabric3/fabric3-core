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
     * Returns the property value. Properties may be single-valued or multi-valued. Values are stored as child nodes of the root.
     *
     * @return the property value
     */
    public Document getXmlValue() {
        return xmlValue;
    }

    /**
     * Returns the property value key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
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

    /**
     * Returns true if the property is required.
     *
     * @return true if the property is required
     */
    public boolean isRequired() {
        return required;
    }

    private PhysicalProperty() {
    }

    public static class Builder {
        private PhysicalProperty property;

        public static Builder newBuilder(String name) {
            return new Builder(name);
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

        public PhysicalProperty build() {
            return property;
        }

        private Builder(String name) {
            property = new PhysicalProperty();
            property.name = name;
        }
    }
}
