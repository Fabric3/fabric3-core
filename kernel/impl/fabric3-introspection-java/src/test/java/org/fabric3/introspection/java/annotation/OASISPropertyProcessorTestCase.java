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
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.oasisopen.sca.annotation.Property;

@SuppressWarnings("unchecked")
public class OASISPropertyProcessorTestCase extends TestCase {
    private OASISPropertyProcessor processor;
    private InjectingComponentType componentType;

    public void testInvalidMethodAccessor() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setRequiredProperty", String.class);
        Property annotation = method.getAnnotation(Property.class);
        IntrospectionContext context = new DefaultIntrospectionContext();

        processor.visitMethod(annotation, method, TestPrivateClass.class, componentType, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidFieldAccessor() throws Exception {
        Field field = TestPrivateClass.class.getDeclaredField("requiredProperty");
        Property annotation = field.getAnnotation(Property.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestPrivateClass.class, mapping);

        processor.visitField(annotation, field, TestPrivateClass.class, componentType, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidParameters() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setNoParamsProperty");
        Property annotation = method.getAnnotation(Property.class);
        IntrospectionContext context = new DefaultIntrospectionContext();

        processor.visitMethod(annotation, method, TestPrivateClass.class, componentType, context);
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

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new OASISPropertyProcessor(helper);
        componentType = new InjectingComponentType();
    }
}