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
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Property;

import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.model.type.java.InjectingComponentType;

@SuppressWarnings("unchecked")
public class OASISPropertyProcessorTestCase extends TestCase {
    private OASISPropertyProcessor<Implementation<? extends InjectingComponentType>> processor;


    public void testInvalidMethodAccessor() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setRequiredProperty", String.class);
        Property annotation = method.getAnnotation(Property.class);
        IntrospectionContext context = new DefaultIntrospectionContext();

        processor.visitMethod(annotation, method, TestPrivateClass.class, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidFieldAccessor() throws Exception {
        Field field = TestPrivateClass.class.getDeclaredField("requiredProperty");
        Property annotation = field.getAnnotation(Property.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestPrivateClass.class, mapping);

        processor.visitField(annotation, field, TestPrivateClass.class, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidParameters() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setNoParamsProperty");
        Property annotation = method.getAnnotation(Property.class);
        IntrospectionContext context = new DefaultIntrospectionContext();

        processor.visitMethod(annotation, method, TestPrivateClass.class, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidMethod);
    }

    public static class TestPrivateClass {

        @Property
        private void setNoParamsProperty() {

        }

        @Property
        private void setProperty(String clazz) {

        }

        @Property(required = true)
        private void setRequiredProperty(String clazz) {

        }

        @Property
        private String property;

        @Property(required = true)
        private String requiredProperty;
    }

    public static class TestImplementation extends Implementation {
        private static final long serialVersionUID = 2759280710238779821L;

        public QName getType() {
            return null;
        }

        public ComponentType getComponentType() {
            return new InjectingComponentType();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new OASISPropertyProcessor<Implementation<? extends InjectingComponentType>>(helper);


    }
}