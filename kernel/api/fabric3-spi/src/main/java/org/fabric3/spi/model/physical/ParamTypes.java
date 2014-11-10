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
package org.fabric3.spi.model.physical;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines primitives for use in dynamic classloading.
 */
public class ParamTypes {

    public static final Map<String, Class<?>> PRIMITIVES_TYPES;
    public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_OBJECT;

    static {
        PRIMITIVES_TYPES = new HashMap<>();
        PRIMITIVES_TYPES.put("void", Void.class);
        PRIMITIVES_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVES_TYPES.put("byte", Byte.TYPE);
        PRIMITIVES_TYPES.put("short", Short.TYPE);
        PRIMITIVES_TYPES.put("int", Integer.TYPE);
        PRIMITIVES_TYPES.put("long", Long.TYPE);
        PRIMITIVES_TYPES.put("float", Float.TYPE);
        PRIMITIVES_TYPES.put("double", Double.TYPE);
        PRIMITIVES_TYPES.put("char", Character.TYPE);

        PRIMITIVE_TO_OBJECT = new HashMap<>();
        PRIMITIVE_TO_OBJECT.put(Void.TYPE, Void.class);
        PRIMITIVE_TO_OBJECT.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_TO_OBJECT.put(Byte.TYPE, Byte.class);
        PRIMITIVE_TO_OBJECT.put(Short.TYPE, Short.class);
        PRIMITIVE_TO_OBJECT.put(Integer.TYPE, Integer.class);
        PRIMITIVE_TO_OBJECT.put(Long.TYPE, Long.class);
        PRIMITIVE_TO_OBJECT.put(Float.TYPE, Float.class);
        PRIMITIVE_TO_OBJECT.put(Double.TYPE, Double.class);
        PRIMITIVE_TO_OBJECT.put(Character.TYPE, Character.class);

    }

}
