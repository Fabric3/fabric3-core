/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.spi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines primitives for use in dynamic classloading.
 *
 * @version $Rev$ $Date$
 */
public class ParamTypes {

    public static final Map<String, Class<?>> PRIMITIVES_TYPES;
    public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_OBJECT;

    static {
        PRIMITIVES_TYPES = new HashMap<String, Class<?>>();
        PRIMITIVES_TYPES.put("void", Void.class);
        PRIMITIVES_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVES_TYPES.put("byte", Byte.TYPE);
        PRIMITIVES_TYPES.put("short", Short.TYPE);
        PRIMITIVES_TYPES.put("int", Integer.TYPE);
        PRIMITIVES_TYPES.put("long", Long.TYPE);
        PRIMITIVES_TYPES.put("float", Float.TYPE);
        PRIMITIVES_TYPES.put("double", Double.TYPE);
        PRIMITIVES_TYPES.put("char", Character.TYPE);

        PRIMITIVE_TO_OBJECT = new HashMap<Class<?>, Class<?>>();
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
