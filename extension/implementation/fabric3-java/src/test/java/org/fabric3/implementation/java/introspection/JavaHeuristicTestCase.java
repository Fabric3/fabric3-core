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
 */
package org.fabric3.implementation.java.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JavaHeuristicTestCase extends TestCase {
    private JavaHeuristic heuristic;
    private IntrospectionHelper helper;
    private InjectingComponentType type;
    private JavaContractProcessor contractProcessor;

    @SuppressWarnings({"unchecked"})
    public void testHeuristicMultipleConstructorsWithAnnotation() throws Exception {

        EasyMock.expect(helper.getInjectionMethods(EasyMock.isA(Class.class),
                                                   EasyMock.isA(Collection.class))).andReturn(Collections.<Method>emptySet());
        EasyMock.expect(helper.getInjectionFields(EasyMock.isA(Class.class))).andReturn(Collections.<Field>emptySet());

        helper.getBaseType(EasyMock.isA(Type.class), EasyMock.isA(TypeMapping.class));

        EasyMock.expectLastCall().andReturn(List.class);
        EasyMock.expect(helper.inferType(EasyMock.isA(Class.class), EasyMock.isA(TypeMapping.class))).andReturn(InjectableType.REFERENCE);

        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class),
                                                     EasyMock.isA(InjectingComponentType.class))).andReturn(new JavaServiceContract());
        EasyMock.replay(helper, contractProcessor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        context.addTypeMapping(MultipleCtorsWithAnnotation.class, new TypeMapping());

        heuristic.applyHeuristics(type, MultipleCtorsWithAnnotation.class, context);
        assertEquals(List.class.getName(), type.getConstructor().getParameterTypes().get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void testHeuristicMultipleConstructors() throws Exception {

        EasyMock.expect(helper.getInjectionMethods(EasyMock.isA(Class.class),
                                                   EasyMock.isA(Collection.class))).andReturn(Collections.<Method>emptySet());
        EasyMock.expect(helper.getInjectionFields(EasyMock.isA(Class.class))).andReturn(Collections.<Field>emptySet());

        helper.getBaseType(EasyMock.isA(Type.class), EasyMock.isA(TypeMapping.class));

        EasyMock.expectLastCall().andReturn(List.class);
        EasyMock.expect(helper.inferType(EasyMock.isA(Class.class), EasyMock.isA(TypeMapping.class))).andReturn(InjectableType.REFERENCE);

        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class),
                                                     EasyMock.isA(InjectingComponentType.class))).andReturn(new JavaServiceContract());
        EasyMock.replay(helper, contractProcessor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        context.addTypeMapping(MultipleCtors.class, new TypeMapping());

        heuristic.applyHeuristics(type, MultipleCtors.class, context);
        assertEquals(List.class.getName(), type.getConstructor().getParameterTypes().get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void setUp() throws Exception {
        super.setUp();
        helper = EasyMock.createNiceMock(IntrospectionHelper.class);
        HeuristicProcessor processor = EasyMock.createMock(HeuristicProcessor.class);
        contractProcessor = EasyMock.createMock(JavaContractProcessor.class);
        heuristic = new JavaHeuristic(helper, contractProcessor, processor);

        type = new InjectingComponentType();
    }


    private static class MultipleCtorsWithAnnotation {
        public MultipleCtorsWithAnnotation() {
        }

        public MultipleCtorsWithAnnotation(@Reference List<Object> services) {
        }

    }

    private static class MultipleCtors {
        public MultipleCtors() {
        }

        @Constructor
        public MultipleCtors(@Reference List<Object> services) {
        }

    }

}
