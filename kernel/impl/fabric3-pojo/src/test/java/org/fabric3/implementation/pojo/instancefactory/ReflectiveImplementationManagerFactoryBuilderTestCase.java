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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.instancefactory;

import junit.framework.TestCase;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.pojo.reflection.ReflectiveImplementationManagerFactoryBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.model.type.java.Signature;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ReflectiveImplementationManagerFactoryBuilderTestCase extends TestCase {
    private ReflectiveImplementationManagerFactoryBuilder builder = new ReflectiveImplementationManagerFactoryBuilder(new MockClassLoaderRegistry());
    private ImplementationManagerDefinition definition;
    private Constructor<Foo> constructor;
    private ClassLoader cl;

    /**
     * Verifies an InjectableAttribute is set properly for constructor parameters
     *
     * @throws Exception
     */
    public void testCdiSource() throws Exception {
        ImplementationManagerFactory factory = builder.build(definition, cl);
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

        ImplementationManagerFactory factory = builder.build(definition, cl);
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

        ImplementationManagerFactory factory = builder.build(definition, cl);
        Class<?> clazz = factory.getMemberType(injectable);
        assertEquals(Bar.class, clazz);
    }


    protected void setUp() throws Exception {
        super.setUp();
        cl = getClass().getClassLoader();
        constructor = Foo.class.getConstructor(String.class, Long.class);

        definition = new ImplementationManagerDefinition();
        definition.setImplementationClass(Foo.class.getName());
        definition.setConstructor(new Signature(constructor));
        definition.setInitMethod(new Signature("init"));
        definition.setDestroyMethod(new Signature("destroy"));
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

    private class MockClassLoaderRegistry implements ClassLoaderRegistry {

        public void register(URI id, ClassLoader classLoader) {

        }

        public ClassLoader unregister(URI id) {
            return null;
        }

        public ClassLoader getClassLoader(URI id) {
            return null;
        }

        public Map<URI, ClassLoader> getClassLoaders() {
            return null;
        }

        public Class<?> loadClass(URI classLoaderId, String className) throws ClassNotFoundException {
            return null;
        }

        public Class<?> loadClass(ClassLoader cl, String className) throws ClassNotFoundException {
            return Class.forName(className, true, cl);
        }

        public List<URI> resolveParentUris(ClassLoader cl) {
            return null;
        }
    }
}
