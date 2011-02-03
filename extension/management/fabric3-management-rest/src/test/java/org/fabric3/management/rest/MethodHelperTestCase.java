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

import junit.framework.TestCase;

/**
 * Utilities for converting method names to resource metadata.
 *
 * @version $Rev$ $Date$
 */
public final class MethodHelperTestCase extends TestCase {

    public void testDeleteConvertToPath() throws Exception {
        assertEquals("/foo", MethodHelper.convertToPath("deleteFoo"));
        assertEquals("/delete", MethodHelper.convertToPath("delete"));
    }

    public void testCreateConvertToPath() throws Exception {
        assertEquals("/foo", MethodHelper.convertToPath("createFoo"));
        assertEquals("/create", MethodHelper.convertToPath("create"));
    }

    public void testGetConvertToPath() throws Exception {
        assertEquals("/foo", MethodHelper.convertToPath("getFoo"));
        assertEquals("/get", MethodHelper.convertToPath("get"));
    }

    public void testSetConvertToPath() throws Exception {
        assertEquals("/foo", MethodHelper.convertToPath("setFoo"));
        assertEquals("/set", MethodHelper.convertToPath("set"));
    }

    public void testIsConvertToPath() throws Exception {
        assertEquals("/foo", MethodHelper.convertToPath("isFoo"));
        assertEquals("/is", MethodHelper.convertToPath("is"));
    }

    public void testRandomConvertToPath() throws Exception {
        assertEquals("/something", MethodHelper.convertToPath("something"));
    }

    public void testConvertToVerb() throws Exception {
        assertEquals(Verb.DELETE, MethodHelper.convertToVerb("deleteFoo"));
        assertEquals(Verb.GET, MethodHelper.convertToVerb("getFoo"));
        assertEquals(Verb.POST, MethodHelper.convertToVerb("setFoo"));
        assertEquals(Verb.PUT, MethodHelper.convertToVerb("createFoo"));
        assertEquals(Verb.GET, MethodHelper.convertToVerb("something"));
    }

}
