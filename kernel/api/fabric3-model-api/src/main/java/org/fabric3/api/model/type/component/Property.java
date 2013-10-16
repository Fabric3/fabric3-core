/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.api.model.type.component;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.fabric3.api.model.type.ModelObject;

/**
 * A component type property.
 */
public class Property extends ModelObject<ComponentType> {
    private static final long serialVersionUID = -1930360315004829917L;

    private String name;
    private boolean many;
    private boolean required;
    private Document defaultValue;
    private QName type;
    private QName element;

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
}
