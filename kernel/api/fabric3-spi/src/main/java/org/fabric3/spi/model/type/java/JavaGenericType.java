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
*/
package org.fabric3.spi.model.type.java;

import org.fabric3.model.type.contract.DataType;

/**
 * A Java generic type. The physical type is the raw type, e.g. <code>List</code> for the parameterized type <code>List&lt;String&gt;</code>. The
 * logical type is a {@link JavaTypeInfo} which represents resolved generic type information. The logical type can be used to perform strong type
 * checking that includes the actual types of generic parameters, e.g. a check that can verify <code>List&lt;String&gt;</code> as opposed to just
 * <code>List</code>.
 */
public class JavaGenericType extends JavaType<JavaTypeInfo> {
    private static final long serialVersionUID = -8832071773275935399L;

    public JavaGenericType(JavaTypeInfo info) {
        super(info.getRawType(), info);
    }

    /**
     * Overrides <code>DataType.equals()</code> to implement equality between unbound generic types or generc types with <code>java.lang.Object</code>
     * as an upper bound.
     *
     * @param o the object to test for equality
     * @return if the objects are equal
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataType)) {
            return false;
        }
        DataType other = (DataType) o;
        if (!getPhysical().equals(other.getPhysical())) {
            return false;
        }

        if (other instanceof JavaClass) {
            boolean bound = false;  // unbound parameters are equivalent to non-generic types
            for (JavaTypeInfo info : getLogical().getParameterTypesInfos()) {
                if (!Object.class.equals(info.getRawType())) {
                    bound = true;
                    break;
                }
            }
            if (!bound) {
                JavaClass<?> otherClazz = (JavaClass<?>) other;
                return getLogical().getRawType().equals(otherClazz.getLogical());
            }
        }
        return getLogical().equals(other.getLogical());
    }


}
