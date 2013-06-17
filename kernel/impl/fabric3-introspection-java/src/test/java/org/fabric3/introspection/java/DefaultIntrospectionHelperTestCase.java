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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.model.type.component.AbstractService;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;

/**
 *
 */
public class DefaultIntrospectionHelperTestCase extends TestCase {
    private DefaultIntrospectionHelper helper;
    private TypeMapping boundMapping;
    private TypeMapping baseMapping;

    private static interface SuperInterface {
    }

    private static interface ServiceInterface extends SuperInterface {
    }

    private static interface SubInterface extends ServiceInterface {
    }

    private static class Base {
    }

    private static class BaseWithInterface implements ServiceInterface {
    }

    private static class ExtendsBaseWithInterface extends BaseWithInterface {
    }

    private static class ExtendsBaseWithSubInterface extends BaseWithInterface implements SubInterface {
    }

    private static class ExtendsBase extends Base {
    }

    private static class BaseTypes<T extends Base> {
        public T t;
        public Collection<String> stringCollection;
        public Map<String, Integer> intMap;

        public T[] tArray;
        public Collection<T> tCollection;
        public Map<String, T> tMap;
    }

    private static class BoundTypes extends BaseTypes<ExtendsBase> {
    }

    public void testTypeMappingBoundTypes() {
        assertEquals(ExtendsBase.class, boundMapping.getActualType(getType(BaseTypes.class, "t")));
    }

    public void testBaseType() {
        assertEquals(String.class, helper.getBaseType(String.class, baseMapping));
        assertEquals(int.class, helper.getBaseType(int.class, baseMapping));
        assertEquals(int.class, helper.getBaseType(Integer.TYPE, baseMapping));
        assertEquals(Integer.class, helper.getBaseType(Integer.class, baseMapping));

        assertEquals(int.class, helper.getBaseType(int[].class, baseMapping));
        assertEquals(String.class, helper.getBaseType(String[].class, baseMapping));

        assertEquals(String.class, helper.getBaseType(getType(BaseTypes.class, "stringCollection"), baseMapping));
        assertEquals(Integer.class, helper.getBaseType(getType(BaseTypes.class, "intMap"), baseMapping));

        assertEquals(Base.class, helper.getBaseType(getType(BaseTypes.class, "tArray"), baseMapping));
        assertEquals(Base.class, helper.getBaseType(getType(BaseTypes.class, "tCollection"), baseMapping));
        assertEquals(Base.class, helper.getBaseType(getType(BaseTypes.class, "tMap"), baseMapping));
    }

    public void testBoundTypes() {
        assertEquals(ExtendsBase.class, helper.getBaseType(getType(BoundTypes.class, "tArray"), boundMapping));
        assertEquals(ExtendsBase.class, helper.getBaseType(getType(BoundTypes.class, "tCollection"), boundMapping));
    }

    protected Type getType(Class<?> type, String fieldName) {
        try {
            return type.getField(fieldName).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new AssertionError(fieldName);
        }
    }

    public void testImplementedInterfacesWithNoInterfaces() {
        assertTrue(helper.getImplementedInterfaces(Base.class).isEmpty());
        assertTrue(helper.getImplementedInterfaces(ExtendsBase.class).isEmpty());
    }

    public void testImplementedInterfaces() {
        assertEquals(Collections.singleton(ServiceInterface.class), helper.getImplementedInterfaces(BaseWithInterface.class));
        assertEquals(Collections.singleton(ServiceInterface.class), helper.getImplementedInterfaces(ExtendsBaseWithInterface.class));
        assertEquals(Collections.singleton(SubInterface.class), helper.getImplementedInterfaces(ExtendsBaseWithSubInterface.class));
    }

    private abstract static class InjectionBase {
        // these fields should be found
        public int publicBase;
        protected int protectedBase;

        // these fields should not
        int packageBase;
        private int privateBase;
        public static int staticInt;
        public final int finalBase = 0;

        // these methods should be found
        public void setPublicBase(int value) {
        }

        public void setPublicOverride(int value) {
        }

        protected void setProtectedBase(int value) {
        }

        // these methods should not
        void setPackageBase(int value) {
        }

        private void setPrivateBase(int value) {
        }

        public static void setStatic(int value) {
        }

        public abstract void setAbstract(int value);

        public void set(int value) {
        }

        public void setNoValue() {
        }

        public int setIntReturn(int value) {
            return 0;
        }
    }

    private static class InjectionSubClass extends InjectionBase {
        public int publicSub;
        protected Object protectedBase; // field that obscures the one in the superclass (yuck)

        public void setAbstract(int value) {
        }

        public void setPublicOverride(int value) {
        }
    }

    private static interface InterfaceWithSetter {
        void setFromInterface(int value);
    }

    private static class InjectionWithInterface implements InterfaceWithSetter {
        // this should not be included
        public void setFromInterface(int value) {
        }
    }

    public void testGetInjectionFields() throws NoSuchFieldException {
        Set<Field> expected = new HashSet<Field>();
        expected.add(InjectionBase.class.getDeclaredField("publicBase"));
        expected.add(InjectionBase.class.getDeclaredField("protectedBase"));
        assertEquals(expected, helper.getInjectionFields(InjectionBase.class));
    }

    public void testGetInjectionFieldsOnSubclass() throws NoSuchFieldException {
        Set<Field> expected = new HashSet<Field>();
        expected.add(InjectionBase.class.getDeclaredField("publicBase"));
        expected.add(InjectionSubClass.class.getDeclaredField("publicSub"));
        expected.add(InjectionSubClass.class.getDeclaredField("protectedBase"));
        assertEquals(expected, helper.getInjectionFields(InjectionSubClass.class));
    }

    public void testGetInjectionMethods() throws NoSuchMethodException {
        Set<Method> expected = new HashSet<Method>();
        expected.add(InjectionBase.class.getDeclaredMethod("setPublicBase", int.class));
        expected.add(InjectionBase.class.getDeclaredMethod("setPublicOverride", int.class));
        expected.add(InjectionBase.class.getDeclaredMethod("setProtectedBase", int.class));
        Collection<AbstractService> services = Collections.emptySet();
        assertEquals(expected, helper.getInjectionMethods(InjectionBase.class, services));
    }

    public void testGetInjectionMethodsOnSubclass() throws NoSuchMethodException {
        Set<Method> expected = new HashSet<Method>();
        expected.add(InjectionBase.class.getDeclaredMethod("setPublicBase", int.class));
        expected.add(InjectionBase.class.getDeclaredMethod("setProtectedBase", int.class));
        expected.add(InjectionSubClass.class.getDeclaredMethod("setPublicOverride", int.class));
        expected.add(InjectionSubClass.class.getDeclaredMethod("setAbstract", int.class));
        Collection<AbstractService> services = Collections.emptySet();
        assertEquals(expected, helper.getInjectionMethods(InjectionSubClass.class, services));
    }

    public void testGetInjectionMethodsExcludesService() throws NoSuchMethodException {
        Set<Method> expected = Collections.emptySet();
        Set<AbstractService> services = new HashSet<AbstractService>();
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(InterfaceWithSetter.class, mapping);

        ServiceContract contract = new JavaContractProcessorImpl(helper).introspect(InterfaceWithSetter.class, context);
        AbstractService definition = new ServiceDefinition("InterfaceWithSetter", contract);
        services.add(definition);
        assertEquals(expected, helper.getInjectionMethods(InjectionWithInterface.class, services));
    }

    protected void setUp() throws Exception {
        super.setUp();
        helper = new DefaultIntrospectionHelper();
        baseMapping = new TypeMapping();
        helper.resolveTypeParameters(BaseTypes.class, baseMapping);
        boundMapping = new TypeMapping();
        helper.resolveTypeParameters(BoundTypes.class, boundMapping);
    }
}
