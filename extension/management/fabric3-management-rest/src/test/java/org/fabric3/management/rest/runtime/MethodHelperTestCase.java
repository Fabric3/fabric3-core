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

import junit.framework.TestCase;

import org.fabric3.management.rest.spi.Verb;

/**
 * Utilities for converting method names to resource metadata.
 */
public class MethodHelperTestCase extends TestCase {

    public void testDeleteConvertToPath() throws Exception {
        assertEquals("foo", MethodHelper.convertToPath("deleteFoo"));
        assertEquals("delete", MethodHelper.convertToPath("delete"));
    }

    public void testCreateConvertToPath() throws Exception {
        assertEquals("foo", MethodHelper.convertToPath("createFoo"));
        assertEquals("create", MethodHelper.convertToPath("create"));
    }

    public void testGetConvertToPath() throws Exception {
        assertEquals("foo", MethodHelper.convertToPath("getFoo"));
        assertEquals("get", MethodHelper.convertToPath("get"));
    }

    public void testSetConvertToPath() throws Exception {
        assertEquals("foo", MethodHelper.convertToPath("setFoo"));
        assertEquals("set", MethodHelper.convertToPath("set"));
    }

    public void testIsConvertToPath() throws Exception {
        assertEquals("foo", MethodHelper.convertToPath("isFoo"));
        assertEquals("is", MethodHelper.convertToPath("is"));
    }

    public void testRandomConvertToPath() throws Exception {
        assertEquals("something", MethodHelper.convertToPath("something"));
    }

    public void testConvertToVerb() throws Exception {
        assertEquals(Verb.DELETE, MethodHelper.convertToVerb("deleteFoo"));
        assertEquals(Verb.GET, MethodHelper.convertToVerb("getFoo"));
        assertEquals(Verb.POST, MethodHelper.convertToVerb("setFoo"));
        assertEquals(Verb.PUT, MethodHelper.convertToVerb("createFoo"));
        assertEquals(Verb.GET, MethodHelper.convertToVerb("something"));
    }

}
