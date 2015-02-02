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
package org.fabric3.api.model.type.component;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;
import org.w3c.dom.Document;

/**
 * A component type property.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class Property extends ModelObject<ComponentType> {
    private String name;
    private boolean many;
    private boolean required;
    private Document defaultValue;
    private QName type;
    private QName element;
    private String source;
    private Map<String, String> namespaces = Collections.emptyMap();

    public Property(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the property.
     *
     * @return the name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the property.
     *
     * @param name the name of the property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether the property is many-valued or single-valued.
     *
     * @return true if the property is many-valued
     */
    public boolean isMany() {
        return many;
    }

    /**
     * Sets whether the property is many-valued or single-valued.
     *
     * @param many true if the property is many-valued
     */
    public void setMany(boolean many) {
        this.many = many;
    }

    /**
     * Returns whether the component definition must supply a value for this property.
     *
     * @return true if the component definition must supply a value
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether the component definition must supply a value for this property.
     *
     * @param required true if the component definition must supply a value
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns the default property value as a DOM.
     *
     * @return the default property value
     */
    public Document getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default property values.
     *
     * @param value the default property values
     */
    public void setDefaultValue(Document value) {
        this.defaultValue = value;
    }

    /**
     * Returns the property XSD type or null if it is not an XSD type.
     *
     * @return the XSD type
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the XSD type
     *
     * @param type the XSD type
     */
    public void setType(QName type) {
        this.type = type;
    }

    /**
     * Gets the property XSD element or null if the property is not an XSD element type
     *
     * @return XSD element
     */
    public QName getElement() {
        return element;
    }

    /**
     * Sets the property XSD element
     *
     * @param element XSD element
     */
    public void setElement(QName element) {
        this.element = element;
    }

    /**
     * Returns the property source
     *
     * @return the property source
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the property source
     *
     * @param source the property source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Adds a namespace used to resolve a property source.
     *
     * @param prefix the namespace prefix
     * @param uri    the namespace uri
     */
    public void addNamespace(String prefix, String uri) {
        if (namespaces.isEmpty()) {
            namespaces = new HashMap<String, String>();
        }
        namespaces.put(prefix, uri);
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }
}
