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
package org.fabric3.management.rest.runtime;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class ParamDeserializer {

    public Object deserialize(String value, Method method) throws IOException {
        if (method.getParameterTypes().length != 1) {
            throw new IOException("Invalid number of parameters: " + method);
        }
        Class<?> type = method.getParameterTypes()[0];
        if (String.class.equals(type)) {
            return value;
        } else if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
            return Integer.parseInt(value);
        } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
            return Long.parseLong(value);
        } else if (Double.class.equals(type) || Double.TYPE.equals(type)) {
            return Double.parseDouble(value);
        } else if (Short.class.equals(type) || Short.TYPE.equals(type)) {
            return Short.parseShort(value);
        } else if (Float.class.equals(type) || Float.TYPE.equals(type)) {
            return Float.parseFloat(value);
        }
        throw new IOException("Unsupported parameter type: " + method);
    }
}
