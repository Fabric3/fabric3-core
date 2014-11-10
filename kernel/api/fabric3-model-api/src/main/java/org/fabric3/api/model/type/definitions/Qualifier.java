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
package org.fabric3.api.model.type.definitions;

import java.io.Serializable;

/**
 * A qualifier element in an intent definition.
 */
public class Qualifier implements Serializable {
    private static final long serialVersionUID = -7431370537858001929L;
    private String name;
    private boolean defaultQualifier;

    public Qualifier(String name, boolean defaultQualifier) {
        this.name = name;
        this.defaultQualifier = defaultQualifier;
    }

    /**
     * Returns the qualifier name.
     *
     * @return the qualifier name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if the qualifier is the default.
     *
     * @return true if the qualifier is the default.
     */
    public boolean isDefault() {
        return defaultQualifier;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Qualifier qualifier = (Qualifier) o;

        return !(name != null ? !name.equals(qualifier.name) : qualifier.name != null);

    }

    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
