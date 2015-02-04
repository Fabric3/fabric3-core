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
package org.fabric3.implementation.pojo.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class ImplementationManagerFactoryBuilderImplTestCase extends TestCase {
    private ImplementationManagerFactoryBuilderImpl builder;
    private ImplementationManagerDefinition definition;

    /**
     * Verifies an InjectableAttribute is set properly for constructor parameters
     *
     * @throws Exception
     */
    public void testCdiSource() throws Exception {
        ImplementationManagerFactory factory = builder.build(definition);
        assertEquals(String.class, factory.getMemberType(new Injectable(InjectableType.PROPERTY, "a")));
    }

    /**
     * Verifies an InjectableAttribute is set properly for protected fields
     *
     * @throws Exception
     */
    public void testProtectedFieldInjectionSource() throws Exception {
        Injectable injectable = new Injectable(InjectableType.REFERENCE, "xyz");
        Field field = Foo.class.getDeclaredField("xyz");
        InjectionSite injectionSite = new FieldInjectionSite(field);
        definition.getPostConstruction().put(injectionSite, injectable);

        ImplementationManagerFactory factory = builder.build(definition);
        Class<?> clazz = factory.getMemberType(injectable);
        assertEquals(Bar.class, clazz);
    }

    /**
     * Verifies an InjectableAttribute is set properly for setter methods
     *
     * @throws Exception
     */
    public void testMethodInjectionSource() throws Exception {
        Injectable injectable = new Injectable(InjectableType.REFERENCE, "abc");
        Method method = Foo.class.getMethod("setAbc", Bar.class);
        InjectionSite injectionSite = new MethodInjectionSite(method, 0);
        definition.getPostConstruction().put(injectionSite, injectable);

        ImplementationManagerFactory factory = builder.build(definition);
        Class<?> clazz = factory.getMemberType(injectable);
        assertEquals(Bar.class, clazz);
    }

    protected void setUp() throws Exception {
        super.setUp();

        ReflectionFactory reflectionFactory = EasyMock.createNiceMock(ReflectionFactory.class);
        LifecycleInvoker invoker = EasyMock.createMock(LifecycleInvoker.class);
        EasyMock.expect(reflectionFactory.createLifecycleInvoker(EasyMock.isA(Method.class))).andReturn(invoker);
        EasyMock.replay(reflectionFactory);

        builder = new ImplementationManagerFactoryBuilderImpl(reflectionFactory);
        Constructor<Foo> constructor = Foo.class.getConstructor(String.class, Long.class);

        definition = new ImplementationManagerDefinition();
        definition.setImplementationClass(Foo.class);
        definition.setConstructor(constructor);
        definition.setInitMethod(Foo.class.getMethod("init"));
        definition.setDestroyMethod(Foo.class.getMethod("destroy"));
        Map<InjectionSite, Injectable> construction = definition.getConstruction();
        construction.put(new ConstructorInjectionSite(constructor, 0), new Injectable(InjectableType.PROPERTY, "a"));
        construction.put(new ConstructorInjectionSite(constructor, 1), new Injectable(InjectableType.REFERENCE, "b"));
    }

    public static class Foo {

        protected Bar xyz;

        public Foo(String a, Long b) {
        }

        public void setAbc(Bar abc) {
        }

        public void init() {
        }

        public void destroy() {
        }

    }

    public static class Bar {

    }

}
