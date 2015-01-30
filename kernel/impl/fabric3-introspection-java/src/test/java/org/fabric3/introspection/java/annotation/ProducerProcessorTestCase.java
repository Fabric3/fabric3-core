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
import java.util.Collections;

import junit.framework.TestCase;
import org.fabric3.api.annotation.Producer;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;

public class ProducerProcessorTestCase extends TestCase {
    private ProducerProcessor processor;
    private InjectingComponentType componentType;

    public void testMethod() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("setProducer", TestProducer.class);
        Producer annotation = method.getAnnotation(Producer.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());
        assertTrue(componentType.getProducers().containsKey("producer"));
    }

    public void testField() throws Exception {
        Field field = TestClass.class.getDeclaredField("producer");
        Producer annotation = field.getAnnotation(Producer.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitField(annotation, field, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());
        assertTrue(componentType.getProducers().containsKey("producer"));
    }

    public void testName() throws Exception {
        Field field = TestClass.class.getDeclaredField("producer2");
        Producer annotation = field.getAnnotation(Producer.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitField(annotation, field, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());
        assertTrue(componentType.getProducers().containsKey("foo"));
    }

    public static class TestClass {

        @Producer
        public void setProducer(TestProducer clazz) {

        }

        @Producer
        public TestProducer producer;

        @Producer("foo")
        public TestProducer producer2;

    }

    public static interface TestProducer {

        void send(String message);
    }

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        final JavaServiceContract contract = new JavaServiceContract(TestProducer.class);
        contract.setOperations(Collections.singletonList(new Operation("test", null, null, null)));

        JavaContractProcessor contractProcessor = new JavaContractProcessor() {


            public JavaServiceContract introspect(Class<?> interfaze, IntrospectionContext context, ModelObject... modelObjects) {
                return contract;
            }

            public JavaServiceContract introspect(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context, ModelObject... modelObjects) {
                return contract;
            }
        };
        processor = new ProducerProcessor(contractProcessor, helper);
        componentType = new InjectingComponentType();

    }
}