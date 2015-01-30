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
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Callback;

@SuppressWarnings("unchecked")
public class OASISCallbackProcessorTestCase extends TestCase {
    private OASISCallbackProcessor processor;
    private InjectingComponentType componentType;

    public void testInvalidMethodAccessor() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setCallback", TestPrivateClass.class);
        Callback annotation = method.getAnnotation(Callback.class);
        IntrospectionContext context = new DefaultIntrospectionContext();

        processor.visitMethod(annotation, method, TestPrivateClass.class, componentType, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidFieldAccessor() throws Exception {
        Field field = TestPrivateClass.class.getDeclaredField("callbackField");
        Callback annotation = field.getAnnotation(Callback.class);
        IntrospectionContext context = new DefaultIntrospectionContext();

        processor.visitField(annotation, field, TestPrivateClass.class, componentType, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }


    public static class TestPrivateClass {
        @Callback
        private void setCallback(TestPrivateClass clazz) {

        }

        @Callback
        private TestPrivateClass callbackField;

    }


    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        final JavaServiceContract contract = new JavaServiceContract(Implementation.class);

        JavaContractProcessor contractProcessor = new JavaContractProcessor() {

            public JavaServiceContract introspect(Class<?> interfaze, IntrospectionContext context, ModelObject... modelObjects) {
                return contract;
            }

            public JavaServiceContract introspect(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context, ModelObject... modelObjects) {
                return contract;
            }
        };
        processor = new OASISCallbackProcessor(contractProcessor, helper);
        componentType = new InjectingComponentType();

    }
}