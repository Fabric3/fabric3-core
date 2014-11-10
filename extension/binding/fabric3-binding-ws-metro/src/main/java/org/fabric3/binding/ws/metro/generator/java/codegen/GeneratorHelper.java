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
package org.fabric3.binding.ws.metro.generator.java.codegen;

import java.lang.reflect.Method;

/**
 *
 */
public abstract class GeneratorHelper {

    public static String getSignature(Method m) {
        StringBuilder sb = new StringBuilder("(");
        Class[] parameters = m.getParameterTypes();
        for (Class parameter : parameters) {
            sb.append(getSignature(parameter));
        }
        sb.append(')');
        sb.append(getSignature(m.getReturnType()));
        return sb.toString();
    }

    public static String getSignature(Class clazz) {
        if (clazz == Void.TYPE) {
            return "V";
        }
        if (clazz == Byte.TYPE) {
            return "B";
        } else if (clazz == Character.TYPE) {
            return "C";
        } else if (clazz == Double.TYPE) {
            return "D";
        } else if (clazz == Float.TYPE) {
            return "F";
        } else if (clazz == Integer.TYPE) {
            return "I";
        } else if (clazz == Long.TYPE) {
            return "J";
        } else if (clazz == Short.TYPE) {
            return "S";
        } else if (clazz == Boolean.TYPE) {
            return "Z";
        } else if (!clazz.getName().startsWith("[")) {
            // object
            return "L" + clazz.getName().replace('.', '/') + ";";
        } else {
            // array
            return clazz.getName().replace('.', '/');
        }
    }

}