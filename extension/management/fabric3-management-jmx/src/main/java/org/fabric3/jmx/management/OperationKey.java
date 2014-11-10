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
package org.fabric3.jmx.management;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 */
public class OperationKey {
    private String name;
    private String description;
    private String[] params;
    private int hashCode;

    public OperationKey(String name, String[] params) {
        this.name = name;
        this.params = params;
        hashCode = 31 * this.name.hashCode() + Arrays.hashCode(this.params);
    }

    public OperationKey(Method method, String description) {
        this.description = description;
        this.name = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        this.params = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = paramTypes[i].getName();
        }
        hashCode = 31 * this.name.hashCode() + Arrays.hashCode(this.params);
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        StringBuilder sig = new StringBuilder();
        sig.append(name).append('(');
        if (params != null && params.length > 0) {
            sig.append(params[0]);
            for (int i = 1; i < params.length; i++) {
                sig.append(',').append(params[i]);
            }
        }
        sig.append(')');
        return sig.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationKey that = (OperationKey) o;

        if (!name.equals(that.name)) {
            return false;
        } else if (params == null && that.params == null) {
            return true;
        }
        return Arrays.equals(params, that.params);

    }

    public int hashCode() {
        return hashCode;
    }
}
