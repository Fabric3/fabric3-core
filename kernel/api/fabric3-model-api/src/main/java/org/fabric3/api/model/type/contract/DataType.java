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

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * Representation of a user-supplied data type comprising a abstract logical form and a runtime-specific physical form. The logical form describes an
 * abstract type in some arbitrary type system such as XML Schema type or Java Classes. It describes the type of data the user is expecting to use.
 * The physical form describes the representation of that logical data actually used by the runtime. This may describe a Java Object (i.e. the
 * physical form would be the Java Type of that Object typically a Class) or it may describe a surrogate for that Object such as a stream.
 *
 * @param <L> the type of identifier for the logical type system used by this DataType (such as an XML QName or Java Class)
 */
public abstract class DataType<L> implements Serializable {
    private static final long serialVersionUID = 1848442023940979720L;
    private Class<?> physical;
    private L logical;

    /**
     * Construct a data type specifying the physical and logical types.
     *
     * @param physical the physical class used by the runtime
     * @param logical  the logical type identifier
     */
    public DataType(Class<?> physical, L logical) {
        assert physical != null && logical != null;
        this.physical = physical;
        this.logical = logical;
    }

    /**
     * Returns the physical type used by the runtime.
     *
     * @return the physical type used by the runtime
     */
    public Class<?> getPhysical() {
        return physical;
    }

    /**
     * Returns the logical type identifier.
     *
     * @return the logical type identifier
     */
    public L getLogical() {
        return logical;
    }

    /**
     * Returns the XML Schema type or null if this data type cannot be mapped to the Schema type system
     *
     * @return the XML Schema type as a qualified name or null
     */
    public QName getXsdType() {
        return null;
    }

    public int hashCode() {
        return physical.hashCode() + 31 * logical.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataType other = (DataType) o;
        return logical.equals(other.logical) && physical.equals(other.physical);
    }

    public String toString() {
        return "[" + logical + "(" + physical + ")]";
    }
}
