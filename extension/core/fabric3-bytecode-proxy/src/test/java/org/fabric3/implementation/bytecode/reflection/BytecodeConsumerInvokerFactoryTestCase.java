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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.bytecode.reflection;

import java.lang.reflect.Method;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 *
 */
public class BytecodeConsumerInvokerFactoryTestCase extends TestCase {
    private BytecodeConsumerInvokerFactory factory;

    public void testStringInvoke() throws Exception {
        Method method = StringTarget.class.getMethod("invoke", String.class);
        ConsumerInvoker invoker = factory.createInvoker(method);
        StringTarget target = new StringTarget();

        assertEquals("test", invoker.invoke(target, "test"));
    }

    public void testTypeInvoke() throws Exception {
        Method method = TypeTarget.class.getMethod("invoke", Foo.class);
        ConsumerInvoker invoker = factory.createInvoker(method);
        TypeTarget target = new TypeTarget();
        Foo foo = new Foo();

        assertEquals(foo, invoker.invoke(target, foo));
    }

    public void testMultiTypeInvoke() throws Exception {
        Method method = MultiTypeTarget.class.getMethod("invoke", Foo.class, Foo.class);
        ConsumerInvoker invoker = factory.createInvoker(method);
        MultiTypeTarget target = new MultiTypeTarget();
        Foo foo = new Foo();

        assertEquals(foo, invoker.invoke(target, new Foo[]{foo, foo}));
    }

    public void testMultiPrimitiveTypeInvoke() throws Exception {
        Method method = MultiPrimitiveTypeTarget.class.getMethod("invoke", Double.TYPE, Double.TYPE);
        ConsumerInvoker invoker = factory.createInvoker(method);
        MultiPrimitiveTypeTarget target = new MultiPrimitiveTypeTarget();

        assertEquals("test", invoker.invoke(target, new Object[]{1d, 1d}));
    }

    public void testNoArgsInvoke() throws Exception {
        Method method = NoArgsTarget.class.getMethod("invoke");
        ConsumerInvoker invoker = factory.createInvoker(method);
        NoArgsTarget target = new NoArgsTarget();
        invoker.invoke(target, null);
        assertTrue(target.invoked);
    }

    public void testIntPrimitiveInvoke() throws Exception {
        Method method = PrimitiveTypeTarget.class.getMethod("invoke", Integer.TYPE);
        ConsumerInvoker invoker = factory.createInvoker(method);
        PrimitiveTypeTarget target = new PrimitiveTypeTarget();
        assertEquals(1, invoker.invoke(target, 1));
    }

    public void testBooleanPrimitiveInvoke() throws Exception {
        Method method = PrimitiveTypeTarget.class.getMethod("invoke", Boolean.TYPE);
        ConsumerInvoker invoker = factory.createInvoker(method);
        PrimitiveTypeTarget target = new PrimitiveTypeTarget();
        assertTrue((Boolean) invoker.invoke(target, true));
    }

    public void testDoublePrimitiveInvoke() throws Exception {
        Method method = PrimitiveTypeTarget.class.getMethod("invoke", Double.TYPE);
        ConsumerInvoker invoker = factory.createInvoker(method);
        PrimitiveTypeTarget target = new PrimitiveTypeTarget();
        assertEquals(2d, invoker.invoke(target, 2d));
    }

    public void setUp() throws Exception {
        super.setUp();

        ClassLoaderRegistry classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader());
        EasyMock.replay(classLoaderRegistry);

        factory = new BytecodeConsumerInvokerFactory(classLoaderRegistry);
    }

    public class StringTarget {

        public String invoke(String message) {
            return message;
        }
    }

    public class TypeTarget {

        public Foo invoke(Foo message) {
            return message;
        }
    }

    public class MultiTypeTarget {

        public Foo invoke(Foo param1, Foo param2) {
            return param1;
        }
    }

    public class MultiPrimitiveTypeTarget {

        public String invoke(double param1, double param2) {
            return "test";
        }
    }

    public class Foo {

    }

    public class NoArgsTarget {
        public boolean invoked;

        public void invoke() {
            invoked = true;
        }
    }

    public class PrimitiveTypeTarget {

        public boolean invoke() {
            return true;
        }

        public boolean invoke(boolean param) {
            return param;
        }

        public int invoke(int param) {
            return param;
        }

        public double invoke(double param) {
            return param;
        }
    }

}

