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
package org.fabric3.spi.model.type.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 *
 */
public class JavaTypeInfoTestCase extends TestCase {

    public void testToString() {
        JavaTypeInfo key = new JavaTypeInfo(String.class);
        JavaTypeInfo param = new JavaTypeInfo(String.class);
        List<JavaTypeInfo> params = new ArrayList<>();
        params.add(param);
        JavaTypeInfo value = new JavaTypeInfo(Class.class, params);
        List<JavaTypeInfo> mapParams = new ArrayList<>();
        mapParams.add(key);
        mapParams.add(value);
        JavaTypeInfo info = new JavaTypeInfo(Map.class, mapParams);
        assertEquals("java.util.Map<java.lang.String, java.lang.Class<java.lang.String>>", info.toString());
    }

    /**
     * Tests Class<Object> and Class<?>
     */
    public void testUnboundGenericToBoundObjectEquality() {
        JavaTypeInfo param = new JavaTypeInfo(Object.class);
        List<JavaTypeInfo> params = new ArrayList<>();
        params.add(param);
        JavaTypeInfo bound = new JavaTypeInfo(Class.class, params);
        JavaTypeInfo unBound = new JavaTypeInfo(Class.class);
        assertEquals(bound, unBound);
        assertEquals(unBound, bound);
    }

    /**
     * Tests Class<?> and Class<?>
     */
    public void testUnboundEquality() {
        JavaTypeInfo unBound1 = new JavaTypeInfo(Class.class);
        JavaTypeInfo unBound2 = new JavaTypeInfo(Class.class);
        assertEquals(unBound1, unBound2);
        assertEquals(unBound2, unBound1);
    }

    /**
     * Tests Class<Object> and Class<String>
     */
    public void testNotEqualTypeParameters() {
        JavaTypeInfo param = new JavaTypeInfo(String.class);
        List<JavaTypeInfo> params = new ArrayList<>();
        params.add(param);
        JavaTypeInfo info = new JavaTypeInfo(Class.class, params);

        JavaTypeInfo param2 = new JavaTypeInfo(Object.class);
        List<JavaTypeInfo> params2 = new ArrayList<>();
        params2.add(param2);
        JavaTypeInfo info2 = new JavaTypeInfo(Class.class, params2);
        assertFalse(info.equals(info2));
        assertFalse(info2.equals(info));
    }

}
