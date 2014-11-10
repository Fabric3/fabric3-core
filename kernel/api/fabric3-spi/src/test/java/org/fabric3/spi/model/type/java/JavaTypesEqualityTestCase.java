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

import junit.framework.TestCase;

/**
 *
 */
public class JavaTypesEqualityTestCase extends TestCase {

    /**
     * Tests Class<String> and Class
     */
    @SuppressWarnings({"EqualsBetweenInconvertibleTypes"})
    public void testBoundJavaGenericTypeToJavaClass() {
        JavaTypeInfo param = new JavaTypeInfo(String.class);
        List<JavaTypeInfo> params = new ArrayList<>();
        params.add(param);
        JavaTypeInfo info = new JavaTypeInfo(Class.class, params);
        JavaGenericType type = new JavaGenericType(info);
        JavaType clazz = new JavaType(Class.class);
        assertFalse(type.equals(clazz));
        assertFalse(clazz.equals(type));
    }

    /**
     * Tests Class<Object> and Class
     */
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes"})
    public void testBoundObjectJavaGenericTypeToJavaClass() {
        JavaTypeInfo param = new JavaTypeInfo(Object.class);
        List<JavaTypeInfo> params = new ArrayList<>();
        params.add(param);
        JavaTypeInfo info = new JavaTypeInfo(Class.class, params);
        JavaGenericType type = new JavaGenericType(info);
        JavaType clazz = new JavaType(Class.class);
        assertEquals(type, clazz);
        assertEquals(clazz, type);
    }

    /**
     * Tests Class<?> and Class
     */
    @SuppressWarnings({"AssertEqualsBetweenInconvertibleTypes"})
    public void testUnboundJavaGenericTypeToJavaClass() {
        JavaTypeInfo unBound = new JavaTypeInfo(Class.class);
        JavaGenericType type = new JavaGenericType(unBound);
        JavaType clazz = new JavaType(Class.class);
        assertEquals(type, clazz);
        assertEquals(clazz, type);
    }

}
