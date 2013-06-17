/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

