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

import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;

public class ConsumerProcessorTestCase extends TestCase {
    private ConsumerProcessor processor;
    private InjectingComponentType componentType;
    private DefaultIntrospectionContext context;

    public void testMethod() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("onEvent", String.class);
        org.fabric3.api.annotation.Consumer annotation = method.getAnnotation(org.fabric3.api.annotation.Consumer.class);
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());

        Map<String, Consumer<ComponentType>> consumers = componentType.getConsumers();
        Consumer<ComponentType> definition = consumers.get("onEvent");
        assertEquals(String.class, definition.getType().getType());
    }

    public void testSequenceMethod() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("onSequenceEvent", String.class);
        org.fabric3.api.annotation.Consumer annotation = method.getAnnotation(org.fabric3.api.annotation.Consumer.class);
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());

        Map<String, Consumer<ComponentType>> consumers = componentType.getConsumers();
        Consumer definition = consumers.get("onSequenceEvent");

        assertEquals(2, definition.getSequence());
    }

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new ConsumerProcessor(helper);
        componentType = new InjectingComponentType();

        context = new DefaultIntrospectionContext();
    }

    public static class TestClass {

        @org.fabric3.api.annotation.Consumer
        public void onEvent(String message) {

        }

        @org.fabric3.api.annotation.Consumer(sequence = 2)
        public void onSequenceEvent(String message) {

        }

    }

}