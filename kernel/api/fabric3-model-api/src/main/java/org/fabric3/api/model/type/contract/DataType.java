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
package org.fabric3.api.model.type.contract;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * Representation of a data type.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public abstract class DataType implements Serializable {
    private static final long serialVersionUID = 1848442023940979720L;
    private Class<?> type;
    private QName xsdType;
    private String databinding;

    /**
     * Constructor.
     *
     * @param type the class used by the runtime for this type
     */
    public DataType(Class<?> type) {
        this.type = type;
    }

    /**
     * Constructor.
     *
     * @param type    the class used by the runtime for this type
     * @param xsdType the XML Schema type for this type
     */
    public DataType(Class<?> type, QName xsdType) {
        this.type = type;
        this.xsdType = xsdType;
    }

    /**
     * Returns the type used by the runtime.
     *
     * @return the type used by the runtime
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the XML Schema type or null if this data type cannot be mapped to the Schema type system
     *
     * @return the XML Schema type as a qualified name or null
     */
    public QName getXsdType() {
        return xsdType;
    }

    /**
     * Sets the XML Schema type for this type
     *
     * @param xsdType the Schema type
     */
    public void setXsdType(QName xsdType) {
        this.xsdType = xsdType;
    }

    /**
     * Returns the databinding type (e.g. JAXB, JSON) or null.
     *
     * @return the databinding type or null.
     */
    public String getDatabinding() {
        return databinding;
    }

    /**
     * Sets the databinding type.
     *
     * @param databinding the databinding type
     */
    public void setDatabinding(String databinding) {
        this.databinding = databinding;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataType dataType = (DataType) o;

        return type.equals(dataType.type);

    }

    public int hashCode() {
        return type.hashCode();
    }

    public String toString() {
        return "[" + type + "]";
    }
}
