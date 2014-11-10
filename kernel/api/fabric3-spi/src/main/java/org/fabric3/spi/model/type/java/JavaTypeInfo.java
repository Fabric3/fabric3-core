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
package org.fabric3.spi.model.type.java;

import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;

/**
 * Represents resolved type information for a Java generic type. Type parameters are resolved and recursively represented as
 * <code>JavaTypeInfo</code>s. Non-generic types have a raw type and empty parameter type information. For example:
 * <pre>
 *  <ul>
 *      <li><code>List&lt;String&gt;</code> is represented as JavaTypeInfo(raw: List, [TypeInfo(raw: String)])
 *      <li><code>List&lt;List&lt;String&gt;&gt</code> is represented as JavaTypeInfo(raw: List, [TypeInfo(raw: List, [TypeInfo(raw: String)])])
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

    public int hashCode() {
        // Hash code must return that of the raw type so JavaGenericType and JavaClass (which uses the logical Class) can return the same hash code.
        // This is necessary for ConcurrentHashMap gets where the equivalence of non-generic and generic unbound types are required.
        return rawType.hashCode();
    }

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
