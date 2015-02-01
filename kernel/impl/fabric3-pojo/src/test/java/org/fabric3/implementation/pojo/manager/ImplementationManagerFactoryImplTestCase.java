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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class ImplementationManagerFactoryImplTestCase extends TestCase {
    private Constructor<?> argConstructor;
    private List<Injectable> ctrNames;
    private Map<InjectionSite, Injectable> sites;
    private ObjectFactory intFactory;
    private ObjectFactory stringFactory;
    private ImplementationManagerFactoryImpl provider;
    private Field intField;
    private Field stringField;
    private Method intSetter;
    private Method stringSetter;
    private Injectable intProperty = new Injectable(InjectableType.PROPERTY, "int");
    private Injectable stringProperty = new Injectable(InjectableType.PROPERTY, "string");
    private ReflectionFactory reflectionFactory;

    public void testNoConstructorArgs() {
        List<Injectable> sources = Collections.emptyList();
        ObjectFactory<?>[] args = provider.getConstructorParameterFactories(sources);
        assertEquals(0, args.length);
    }

    public void testConstructorArgs() {
        ctrNames.add(intProperty);
        ctrNames.add(stringProperty);
        ClassLoader classLoader = Foo.class.getClassLoader();
        EasyMock.replay(reflectionFactory);
        provider = new ImplementationManagerFactoryImpl(URI.create("TestComponent"),
                                                              argConstructor,
                                                              ctrNames,
                                                              sites,
                                                              null,
                                                              null,
                                                              false,
                                                              classLoader,
                                                              reflectionFactory);
        provider.setObjectFactory(intProperty, intFactory);
        provider.setObjectFactory(stringProperty, stringFactory);
        ObjectFactory<?>[] args = provider.getConstructorParameterFactories(ctrNames);
        assertEquals(2, args.length);
        assertSame(intFactory, args[0]);
        assertSame(stringFactory, args[1]);
    }

    public void testFieldInjectors()  {
        sites.put(new FieldInjectionSite(intField), intProperty);
        sites.put(new FieldInjectionSite(stringField), stringProperty);

        Injector mockInjector = EasyMock.createMock(Injector.class);
        EasyMock.expect(reflectionFactory.createInjector(EasyMock.isA(Field.class),EasyMock.isA(ObjectFactory.class))).andReturn(mockInjector).times(2);
        EasyMock.replay(reflectionFactory);

        Collection<Injector<?>> injectors = provider.createInjectorMappings().values();
        assertEquals(2, injectors.size());

        EasyMock.verify(intFactory, stringFactory, reflectionFactory);
    }

    public void testMethodInjectors() {
        sites.put(new MethodInjectionSite(intSetter, 0), intProperty);
        sites.put(new MethodInjectionSite(stringSetter, 0), stringProperty);

        Injector mockInjector = EasyMock.createMock(Injector.class);
        EasyMock.expect(reflectionFactory.createInjector(EasyMock.isA(Method.class),EasyMock.isA(ObjectFactory.class))).andReturn(mockInjector).times(2);
        EasyMock.replay(reflectionFactory);

        Collection<Injector<?>> injectors = provider.createInjectorMappings().values();
        assertEquals(2, injectors.size());

        EasyMock.verify(intFactory, stringFactory, reflectionFactory);
    }

    public void testFactory() throws Fabric3Exception {
        sites.put(new MethodInjectionSite(intSetter, 0), intProperty);
        sites.put(new MethodInjectionSite(stringSetter, 0), stringProperty);

        ObjectFactory<?> mockFactory = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(reflectionFactory.createInstantiator(EasyMock.isA(Constructor.class), EasyMock.isA(ObjectFactory[].class))).andReturn(mockFactory);
        Injector mockInjector = EasyMock.createMock(Injector.class);
        EasyMock.expect(reflectionFactory.createInjector(EasyMock.isA(Method.class),EasyMock.isA(ObjectFactory.class))).andReturn(mockInjector).times(2);
        EasyMock.replay(reflectionFactory);

        ImplementationManager implementationManager = provider.createManager();
        Foo foo = (Foo) implementationManager.newInstance();
        implementationManager.start(foo);

        EasyMock.verify(intFactory, stringFactory, reflectionFactory);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        Constructor<Foo> noArgConstructor = Foo.class.getConstructor();
        argConstructor = Foo.class.getConstructor(int.class, String.class);
        intField = Foo.class.getField("intField");
        stringField = Foo.class.getField("stringField");
        intSetter = Foo.class.getMethod("setIntField", int.class);
        stringSetter = Foo.class.getMethod("setStringField", String.class);
        ctrNames = new ArrayList<>();
        sites = new HashMap<>();
        ClassLoader classLoader = Foo.class.getClassLoader();
        intFactory = EasyMock.createMock(ObjectFactory.class);
        stringFactory = EasyMock.createMock(ObjectFactory.class);
        reflectionFactory = EasyMock.createMock(ReflectionFactory.class);

        provider = new ImplementationManagerFactoryImpl(URI.create("TestComponent"),
                                                              noArgConstructor,
                                                              ctrNames,
                                                              sites,
                                                              null,
                                                              null,
                                                              false,
                                                              classLoader,
                                                              reflectionFactory);

        EasyMock.replay(intFactory, stringFactory);

        provider.setObjectFactory(intProperty, intFactory);
        provider.setObjectFactory(stringProperty, stringFactory);
    }

    public static class Foo {
        public int intField;
        public String stringField;

        public Foo() {
        }

        public Foo(int intField, String stringField) {
            this.intField = intField;
            this.stringField = stringField;
        }

        public void setIntField(int intField) {
            this.intField = intField;
        }

        public void setStringField(String stringField) {
            this.stringField = stringField;
        }
    }
}
