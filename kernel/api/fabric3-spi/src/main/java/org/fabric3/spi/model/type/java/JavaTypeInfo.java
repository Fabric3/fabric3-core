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

import java.util.Collections;
import java.util.List;

import org.fabric3.model.type.ModelObject;

/**
 * Represents resolved type information for a Java generic type. Type parameters are resolved and recursively represented as
 * <code>JavaTypeInfo</code>s. Non-generic types have a raw type and empty paramter type information. For example:
 * <pre>
 *  <ul>
 *      <li><code>List&lt;String&gt;</code> is represented as TypeInfo(raw: List, [TypeInfo(raw: String)])
 *      <li><code>List&lt;List&lt;String&gt;&gt</code> is represented as TypeInfo(raw: List, [TypeInfo(raw: List, [TypeInfo(raw: String)])])
 *  </ul>
 * Note that unbound TypeVariables and WildCardTypes will be evaluated using their raw type.
 */
public class JavaTypeInfo extends ModelObject {
    private static final long serialVersionUID = -9157948376540103018L;
    private Class<?> rawType;
    private List<JavaTypeInfo> parameterTypeInfos;

    /**
     * Constructor for a non-generic type or unbound type.
     *
     * @param rawType the non-generic type or unbound type.
     */
    public JavaTypeInfo(Class<?> rawType) {
        this.rawType = rawType;
    }

    /**
     * Constructor for a generic type.
     *
     * @param rawType            the raw type of the generic type
     * @param parameterTypeInfos the resolved parameter type information for the generic type.
     */
    public JavaTypeInfo(Class<?> rawType, List<JavaTypeInfo> parameterTypeInfos) {
        this.rawType = rawType;
        this.parameterTypeInfos = parameterTypeInfos;
    }

    /**
     * Returns the raw type for a generic type declaration or the type of a non generic. For example, <code>List</code> will be returned for
     * <code>List&lt;String&gt;</code> and <code>Integer</code> will be returned for the non-generic type <code>Integer</code>.
     *
     * @return the raw type
     */
    public Class<?> getRawType() {
        return rawType;
    }

    /**
     * Returns the resolved parameter types or an empty list if the type is a generic or unbound type.  For example, a list of <code>String</code>s
     * will be returned for <code>List&lt;String&gt;</code>.
     *
     * @return the resolved parameter types
     */
    public List<JavaTypeInfo> getParameterTypesInfos() {
        if (parameterTypeInfos == null) {
            return Collections.emptyList();
        }
        return parameterTypeInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaTypeInfo that = (JavaTypeInfo) o;

        // handle the case where one param type is bound to Object and the other is unbounded 
        if (parameterTypeInfos == null && that.parameterTypeInfos != null) {
            boolean allObject = true;
            for (JavaTypeInfo info : that.parameterTypeInfos) {
                if (!Object.class.equals(info.getRawType())) {
                    allObject = false;
                    break;
                }
            }
            if (allObject) {
                if (rawType.equals(that.rawType)) {
                    return true;
                }
            }
        } else if (parameterTypeInfos != null && that.parameterTypeInfos == null) {
            boolean allObject = true;
            for (JavaTypeInfo info : parameterTypeInfos) {
                if (!Object.class.equals(info.getRawType())) {
                    allObject = false;
                    break;
                }
            }
            if (allObject) {
                if (rawType.equals(that.rawType)) {
                    return true;
                }
            }
        }
        return !(parameterTypeInfos != null ? !parameterTypeInfos.equals(that.parameterTypeInfos) : that.parameterTypeInfos != null)
                && rawType.equals(that.rawType);

    }

    @Override
    public int hashCode() {
        // Hash code must return that of the raw type so JavaGenericType and JavaClass (which uses the logical Class) can return the same hash code.
        // This is necessary for ConcurrentHashMap gets where the equivalence of non-generic and generic unbound types are required.
        return rawType.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        write(this, builder);
        return builder.toString();
    }

    private void write(JavaTypeInfo typeInfo, StringBuilder builder) {
        Class<?> rawType = typeInfo.getRawType();
        builder.append(rawType.getName());
        List<JavaTypeInfo> infos = typeInfo.getParameterTypesInfos();
        if (!infos.isEmpty()) {
            builder.append("<");
            for (int i = 0; i < infos.size(); i++) {
                JavaTypeInfo info = infos.get(i);
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(info);
            }
            builder.append(">");
        }
    }
}
