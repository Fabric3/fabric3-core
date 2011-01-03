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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.model.type.java;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
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
