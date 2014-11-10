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
package org.fabric3.management.rest.runtime;

import org.fabric3.management.rest.spi.Verb;

/**
 * Utilities for converting method names to resource metadata.
 */
public final class MethodHelper {

    private MethodHelper() {
    }

    /**
     * Converts a method name to a relative path.
     *
     * @param methodName the method name
     * @return the relative path
     */
    public static String convertToPath(String methodName) {
        if (methodName.length() > 7 && (methodName.startsWith("delete") || (methodName.startsWith("create")))) {
            return methodName.substring(6, 7).toLowerCase() + methodName.substring(7);
        } else if (methodName.length() > 3 && (methodName.startsWith("set") || (methodName.startsWith("get")))) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        } else if (methodName.length() > 2 && (methodName.startsWith("is"))) {
            return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        } else {
            return methodName;
        }
    }

    /**
     * Converts a method name to an HTTP verb, e.g. GET, PUT, DELETE, POST.
     *
     * @param methodName the method name
     * @return the HTTP verb
     */
    public static Verb convertToVerb(String methodName) {
        if (methodName.startsWith("delete")) {
            return Verb.DELETE;
        } else if (methodName.startsWith("set")) {
            return Verb.POST;
        } else if (methodName.startsWith("create")) {
            return Verb.PUT;
        } else {
            return Verb.GET;
        }
    }

}
