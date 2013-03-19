/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
import org.fabric3.implementation.bytecode.proxy.common.BytecodeClassLoader;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.TargetInvoker;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 *
 */
public class BytecodeReflectionFactoryExtensionTestCase extends TestCase {

    private BytecodeReflectionFactoryExtension factory;
    private TargetInvokerFactory targetInvokerFactory;
    private InjectorFactory injectorFactory;
    private LifecycleInvokerFactory lifecycleInvokerFactory;

    public void testCreateTargetInvoker() throws Exception {
        Method method = StringTarget.class.getMethod("invoke", String.class);
        TargetInvoker targetInvoker = EasyMock.createMock(TargetInvoker.class);
        EasyMock.expect(targetInvokerFactory.createTargetInvoker(EasyMock.isA(Method.class), EasyMock.isA(BytecodeClassLoader.class))).andReturn(targetInvoker);

        EasyMock.replay(targetInvokerFactory);
        factory.createTargetInvoker(method);
        EasyMock.verify(targetInvokerFactory);
    }

    public void testCreateLifecycleInvoke() throws Exception {
        Method method = LifecycleClass.class.getMethod("init");

        LifecycleInvoker invoker = EasyMock.createMock(LifecycleInvoker.class);
        EasyMock.expect(lifecycleInvokerFactory.createLifecycleInvoker(EasyMock.isA(Method.class), EasyMock.isA(BytecodeClassLoader.class))).andReturn(invoker);

        EasyMock.replay(lifecycleInvokerFactory);

        factory.createLifecycleInvoker(method);

        EasyMock.verify(lifecycleInvokerFactory);
    }

    public void testCreateInjector() throws Exception {
        Method method = InjectorClass.class.getDeclaredMethod("setField", String.class);
        ObjectFactory objectFactory = EasyMock.createMock(ObjectFactory.class);

        BytecodeInjector injector = new BytecodeInjector() {
            protected void inject(Object instance, Object target) {
            }
        };
        EasyMock.expect(injectorFactory.createInjector(EasyMock.isA(Method.class),
                                                       EasyMock.isA(ObjectFactory.class),
                                                       EasyMock.isA(BytecodeClassLoader.class))).andReturn(injector);

        EasyMock.replay(injectorFactory);

        factory.createInjector(method, objectFactory);

        EasyMock.verify(injectorFactory);
    }

    public void setUp() throws Exception {
        super.setUp();

        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader());
        EasyMock.replay(registry);

        targetInvokerFactory = EasyMock.createMock(TargetInvokerFactory.class);
        injectorFactory = EasyMock.createMock(InjectorFactory.class);
        lifecycleInvokerFactory = EasyMock.createMock(LifecycleInvokerFactory.class);

        factory = new BytecodeReflectionFactoryExtension(targetInvokerFactory, injectorFactory, lifecycleInvokerFactory, registry);
    }

    public class StringTarget {

        public String invoke(String message) {
            return message;
        }
    }

    public class Foo {

    }

    public class LifecycleClass {

        public void init() throws Exception {

        }

        public void error() throws Exception {
            throw new Exception();

        }

        public void destroy() {

        }

    }

    private class InjectorClass {
        protected String field;

        public void setField(String field) {
            this.field = field;
        }
    }

}

