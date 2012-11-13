/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.model.type.component;

import java.net.URI;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.fabric3.model.type.ModelObject;

/**
 * The value of a configured component property.
 */
public class PropertyValue extends ModelObject {
    private static final long serialVersionUID = -1638553201072873854L;
    private String name;
    private String source;
    private URI file;
    private QName type;
    private QName element;
    private Document value;
    private NamespaceContext namespaceContext;
    private PropertyMany many;
    private ComponentDefinition<?> parent;

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
     * @param name      the name of the property
     * @param value     the property value
     * @param many      the property many parameter
     */
    public PropertyValue(String name, Document value, PropertyMany many) {
        this.name = name;
        this.value = value;
        this.many = many;
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
     * Returns the parent component of this property.
     *
     * @return the parent component
     */
    public ComponentDefinition<?> getParent() {
        return parent;
    }

    /**
     * Sets the parent component of this property.
     *
     * @param parent the parent component
     */
    public void setParent(ComponentDefinition<?> parent) {
        this.parent = parent;
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
