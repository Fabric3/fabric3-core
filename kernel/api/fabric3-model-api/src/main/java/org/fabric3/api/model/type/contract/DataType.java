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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.model.type.contract;

import java.io.Serializable;

/**
 * Representation of a data type.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public abstract class DataType implements Serializable {
    private static final long serialVersionUID = 1848442023940979720L;
    private Class<?> type;
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
     * Returns the type used by the runtime.
     *
     * @return the type used by the runtime
     */
    public Class<?> getType() {
        return type;
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
