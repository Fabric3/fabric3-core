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

import junit.framework.TestCase;

/**
 *
 */
public class SignatureTestCase extends TestCase {

    public void testComplexType() throws Exception {
        Signature signature = new Signature("complex", Foo.class.getName());
        assertEquals(Foo.class.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveInt() throws Exception {
        Signature signature = new Signature("primitiveInt", "int");
        assertEquals(Integer.TYPE.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveDouble() throws Exception {
        Signature signature = new Signature("primitiveDouble", "double");
        assertEquals(Double.TYPE.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveBoolean() throws Exception {
        Signature signature = new Signature("primitiveBoolean", "boolean");
        assertEquals(Boolean.TYPE.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveByte() throws Exception {
        Signature signature = new Signature("primitiveByte", "byte");
        assertEquals(Byte.TYPE.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveShort() throws Exception {
        Signature signature = new Signature("primitiveShort", "short");
        assertEquals(Short.TYPE.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveLong() throws Exception {
        Signature signature = new Signature("primitiveLong", "long");
        assertEquals(Long.TYPE.getName(), signature.getParameterTypes().get(0));
    }

    public void testPrimitiveFloat() throws Exception {
        Signature signature = new Signature("primitiveFloat", "float");
        assertEquals(Float.TYPE.getName(), signature.getParameterTypes().get(0));
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
