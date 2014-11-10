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

import org.fabric3.api.model.type.contract.DataType;

/**
 * A Java type.
 */
public class JavaType extends DataType {
    private static final long serialVersionUID = 9025728312058285754L;

    public JavaType(Class<?> type) {
        super(type);
    }

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

        if (other instanceof JavaGenericType) {
            boolean bound = false;  // unbound parameters are equivalent to non-generic
            JavaGenericType otherType = (JavaGenericType) other;
            for (JavaTypeInfo info : otherType.getTypeInfo().getParameterTypesInfos()) {
                if (!Object.class.equals(info.getRawType())) {
                    bound = true;
                    break;
                }
            }
            return !bound && otherType.getTypeInfo().getRawType().equals(getType());
        }
        return getType().equals(other.getType());
    }

}
