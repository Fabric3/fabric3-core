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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.model.type.ModelObject;
import org.w3c.dom.Document;

/**
 * The value of a configured component property.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class PropertyValue extends ModelObject<Component> {
    private static final long serialVersionUID = -1638553201072873854L;
    private String name;
    private String source;
    private URI file;
    private QName type;
    private QName element;
    private Document value;
    private Object instanceValue;
    private NamespaceContext namespaceContext;
    private PropertyMany many;

    /**
     * Constructor specifying the name of a property and the XPath source expression.
     *
     * @param name   the name of the property which this value is for
     * @param source an XPath expression whose result will be the actual value
     */
    public PropertyValue(String name, String source) {
        this.name = name;
        this.source = source;
    }

    /**
     * Constructor specifying the name of a property loaded from an external resource.
     *
     * @param name the name of the property which this value is for
     * @param file A URI that the property value can be loaded from
     */
    public PropertyValue(String name, URI file) {
        this.name = name;
        this.file = file;
    }

    /**
     * Constructor for inline property values.
     *
     * @param name  the name of the property
     * @param value the property value
     * @param many  the property many parameter
     */
    public PropertyValue(String name, Document value, PropertyMany many) {
        this.name = name;
        this.value = value;
        this.many = many;
    }

    /**
     * Constructor for inline property values.
     *
     * @param name the name of the property
     * @param many the property many parameter
     */
    public PropertyValue(String name, PropertyMany many) {
        this.name = name;
        this.many = many;
    }

    /**
     * Constructor for a value that has already been deserialized to an instance.
     *
     * @param name          the name of the property
     * @param instanceValue the property value
     */
    public PropertyValue(String name, Object instanceValue) {
        this.name = name;
        this.instanceValue = instanceValue;
    }

    /**
     * Returns the name of the property that this value is for.
     *
     * @return the name of the property that this value is for
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the property that this value is for.
     *
     * @param name the name of the property that this value is for
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether the property is many-valued or single-valued.
     *
     * @return true if the property is many-valued
     */
    public PropertyMany getMany() {
        return many;
    }

    /**
     * Sets whether the property is many-valued or single-valued.
     *
     * @param many true if the property is many-valued
     */
    public void setMany(PropertyMany many) {
        this.many = many;
    }

    /**
     * Returns an XPath expression that should be evaluated to get the actual property value.
     *
     * @return an XPath expression that should be evaluated to get the actual property value
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets an XPath expression that should be evaluated to get the actual property value.
     *
     * @param source an XPath expression that should be evaluated to get the actual property value
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the location of the resource containing the property value.
     *
     * @return the location of the resource containing the property value
     */
    public URI getFile() {
        return file;
    }

    /**
     * Sets the location of the resource containing the property value
     *
     * @param file the location of the resource containing the property value
     */
    public void setFile(URI file) {
        this.file = file;
    }

    /**
     * Returns the live value of the property.
     *
     * @return the live value of the property
     */
    public Object getInstanceValue() {
        return instanceValue;
    }

    /**
     * Returns the XML value of the property.
     *
     * @return the XML value of the property
     */
    public Document getValue() {
        return value;
    }

    /**
     * Sets the XML values of the property.
     *
     * @param value the XML value of the property
     */
    public void setValue(Document value) {
        this.value = value;
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
     * Returns the namespace context for the value.
     *
     * @return the namespace context
     */
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Sets the namespace context for the value.
     *
     * @param namespaceContext the namespace context
     */
    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }
}
