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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.model.type.java;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 *
 */
public class SignatureTestCase extends TestCase {

    public void testComplexType() throws Exception {
        Method method = Foo.class.getMethod("complex", Foo.class);
        Signature signature = new Signature("complex", Foo.class.getName());
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveInt() throws Exception {
        Method method = Foo.class.getMethod("primitiveInt", Integer.TYPE);
        Signature signature = new Signature("primitiveInt", "int");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveDouble() throws Exception {
        Method method = Foo.class.getMethod("primitiveDouble", Double.TYPE);
        Signature signature = new Signature("primitiveDouble", "double");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveBoolean() throws Exception {
        Method method = Foo.class.getMethod("primitiveBoolean", Boolean.TYPE);
        Signature signature = new Signature("primitiveBoolean", "boolean");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveByte() throws Exception {
        Method method = Foo.class.getMethod("primitiveByte", Byte.TYPE);
        Signature signature = new Signature("primitiveByte", "byte");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveShort() throws Exception {
        Method method = Foo.class.getMethod("primitiveShort", Short.TYPE);
        Signature signature = new Signature("primitiveShort", "short");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveLong() throws Exception {
        Method method = Foo.class.getMethod("primitiveLong", Long.TYPE);
        Signature signature = new Signature("primitiveLong", "long");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveFloat() throws Exception {
        Method method = Foo.class.getMethod("primitiveFloat", Float.TYPE);
        Signature signature = new Signature("primitiveFloat", "float");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    private class Foo {
        public void complex(Foo foo) {

        }

        public void primitiveInt(int i) {

        }

        public void primitiveDouble(double i) {

        }

        public void primitiveBoolean(boolean i) {

        }

        public void primitiveByte(byte i) {

        }

        public void primitiveShort(short i) {

        }

        public void primitiveLong(long i) {

        }

        public void primitiveFloat(float i) {

        }

    }
}
