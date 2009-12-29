/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.contract.DataType;

/**
 * The value of a configured component property.
 *
 * @version $Rev$ $Date$
 */
public class PropertyValue extends ModelObject {
    private static final long serialVersionUID = -1638553201072873854L;
    private String name;
    private String source;
    private URI file;
    private DataType<QName> valueType;
    private List<Document> values;
    private NamespaceContext namespaceContext;

    /**
     * Constructor specifying the name of a property and the XPath source expression.
     *
     * @param name             the name of the property which this value is for
     * @param source           an XPath expression whose result will be the actual value
     */
    public PropertyValue(String name, String source) {
        this.name = name;
        this.source = source;
    }

    /**
     * Constructor specifying the name of a property loaded from an exteral resource.
     *
     * @param name             the name of the property which this value is for
     * @param file             A URI that the property value can be loaded from
     */
    public PropertyValue(String name, URI file) {
        this.name = name;
        this.file = file;
    }

    /**
     * @param name             the name of the property
     * @param valueType        the XML type of the value
     * @param values            the property values
     */
    public PropertyValue(String name, DataType<QName> valueType, List<Document> values) {
        this.name = name;
        this.valueType = valueType;
        this.values = values;
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
    public List<Document> getValues() {
        return values;
    }

    /**
     * Sets the XML values of the property.
     *
     * @param values the XML value of the property
     */
    public void setValues(List<Document> values) {
        this.values = values;
    }

    /**
     * Returns the value's XML Schema type.
     *
     * @return the value's XML Schema type
     */
    public DataType<QName> getValueType() {
        return valueType;
    }

    /**
     * Sets the value's XML Schema type.
     *
     * @param valueType the value's XML Schema type
     */
    public void setValueType(DataType<QName> valueType) {
        this.valueType = valueType;
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
