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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.model.type.java;

import org.fabric3.api.model.type.contract.DataType;

/**
 * A Java generic type. The {@link JavaTypeInfo} represents resolved generic type information.
 */
public class JavaGenericType extends JavaType {
    private static final long serialVersionUID = -8832071773275935399L;

    private JavaTypeInfo info;

    public JavaGenericType(JavaTypeInfo info) {
        super(info.getRawType());
        this.info = info;
    }

    public JavaTypeInfo getTypeInfo() {
        return info;
    }

    /**
     * Overrides <code>DataType.equals()</code> to implement equality between unbound generic types or generic types with <code>java.lang.Object</code> as an
     * upper bound.
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
        if (!getType().equals(other.getType())) {
            return false;
        }

        if (other instanceof JavaType) {
            boolean bound = false;  // unbound parameters are equivalent to non-generic types
            for (JavaTypeInfo info : getTypeInfo().getParameterTypesInfos()) {
                if (!Object.class.equals(info.getRawType())) {
                    bound = true;
                    break;
                }
            }
            if (!bound) {
                JavaType otherClazz = (JavaType) other;
                return getTypeInfo().getRawType().equals(otherClazz.getType());
            } else {
                return other instanceof JavaGenericType && getTypeInfo().equals(((JavaGenericType) other).getTypeInfo());
            }
        }
        return getTypeInfo().getRawType().equals(other.getType());
    }

}
