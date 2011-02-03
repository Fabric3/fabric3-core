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
package org.fabric3.management.rest;

/**
 * Utilities for converting method names to resource metadata.
 *
 * @version $Rev$ $Date$
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
        if (methodName.length() > 6 && methodName.startsWith("delete") || (methodName.startsWith("create"))) {
            return "/" + methodName.substring(6, 7).toLowerCase() + methodName.substring(7);
        } else if (methodName.length() > 3 && (methodName.startsWith("set") || (methodName.startsWith("get")))) {
            return "/" + methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        } else if (methodName.length() > 2 && (methodName.startsWith("is"))) {
            return "/" + methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        } else {
            return "/" + methodName;
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
