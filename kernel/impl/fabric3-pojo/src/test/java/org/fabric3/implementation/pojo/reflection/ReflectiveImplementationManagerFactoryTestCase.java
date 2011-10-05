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
package org.fabric3.implementation.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.implementation.pojo.instancefactory.ImplementationManager;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveImplementationManagerFactoryTestCase extends TestCase {
    private Constructor<?> argConstructor;
    private List<Injectable> ctrNames;
    private Map<InjectionSite, Injectable> sites;
    private ObjectFactory intFactory;
    private ObjectFactory stringFactory;
    private ReflectiveImplementationManagerFactory provider;
    private Field intField;
    private Field stringField;
    private Method intSetter;
    private Method stringSetter;
    private Injectable intProperty = new Injectable(InjectableType.PROPERTY, "int");
    private Injectable stringProperty = new Injectable(InjectableType.PROPERTY, "string");

    public void testNoConstructorArgs() {
        List<Injectable> sources = Collections.emptyList();
        ObjectFactory<?>[] args = provider.getConstructorParameterFactories(sources);
        assertEquals(0, args.length);
    }

    public void testConstructorArgs() {
        ctrNames.add(intProperty);
        ctrNames.add(stringProperty);
        provider = new ReflectiveImplementationManagerFactory(argConstructor,
                                                         ctrNames,
                                                         sites,
                                                         null,
                                                         null,
                                                         false,
                                                         Foo.class.getClassLoader());
        provider.setObjectFactory(intProperty, intFactory);
        provider.setObjectFactory(stringProperty, stringFactory);
        ObjectFactory<?>[] args = provider.getConstructorParameterFactories(ctrNames);
        assertEquals(2, args.length);
        assertSame(intFactory, args[0]);
        assertSame(stringFactory, args[1]);
    }

    public void testFieldInjectors() throws ObjectCreationException {
        sites.put(new FieldInjectionSite(intField), intProperty);
        sites.put(new FieldInjectionSite(stringField), stringProperty);
        Collection<Injector<Object>> injectors = provider.createInjectorMappings().values();
        assertEquals(2, injectors.size());

        Foo foo = new Foo();
        for (Injector<Object> injector : injectors) {
            assertTrue(injector instanceof FieldInjector);
            injector.inject(foo);
        }
        EasyMock.verify(intFactory, stringFactory);
        assertEquals(34, foo.intField);
        assertEquals("Hello", foo.stringField);
    }

    public void testMethodInjectors() throws ObjectCreationException {
        sites.put(new MethodInjectionSite(intSetter, 0), intProperty);
        sites.put(new MethodInjectionSite(stringSetter, 0), stringProperty);
        Collection<Injector<Object>> injectors = provider.createInjectorMappings().values();
        assertEquals(2, injectors.size());

        Foo foo = new Foo();
        for (Injector<Object> injector : injectors) {
            assertTrue(injector instanceof MethodInjector);
            injector.inject(foo);
        }
        EasyMock.verify(intFactory, stringFactory);
        assertEquals(34, foo.intField);
        assertEquals("Hello", foo.stringField);
    }

    public void testFactory() throws ObjectCreationException, InstanceLifecycleException {
        sites.put(new MethodInjectionSite(intSetter, 0), intProperty);
        sites.put(new MethodInjectionSite(stringSetter, 0), stringProperty);
        ImplementationManager implementationManager = provider.createManager();
        WorkContext workContext = new WorkContext();
        Foo foo = (Foo) implementationManager.newInstance(workContext);
        implementationManager.start(foo, workContext);
        EasyMock.verify(intFactory, stringFactory);
        assertEquals(34, foo.intField);
        assertEquals("Hello", foo.stringField);
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
        ctrNames = new ArrayList<Injectable>();
        sites = new HashMap<InjectionSite, Injectable>();
        provider = new ReflectiveImplementationManagerFactory(noArgConstructor,
                                                         ctrNames,
                                                         sites,
                                                         null,
                                                         null,
                                                         false,
                                                         Foo.class.getClassLoader());
        intFactory = EasyMock.createMock(ObjectFactory.class);
        stringFactory = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(intFactory.getInstance()).andReturn(34);
        EasyMock.expect(stringFactory.getInstance()).andReturn("Hello");
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
