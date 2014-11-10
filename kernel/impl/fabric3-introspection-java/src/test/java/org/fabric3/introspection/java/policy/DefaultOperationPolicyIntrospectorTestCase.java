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
package org.fabric3.introspection.java.policy;

import java.lang.annotation.Annotation;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.annotation.security.RolesAllowed;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 *
 */
public class DefaultOperationPolicyIntrospectorTestCase extends TestCase {

    public void testOperationIntrospection() throws Exception {
        JavaContractProcessor contractProcessor = new JavaContractProcessorImpl(new DefaultIntrospectionHelper());

        PolicyAnnotationProcessor processor = EasyMock.createNiceMock(PolicyAnnotationProcessor.class);
        processor.process(EasyMock.isA(Annotation.class), EasyMock.isA(Operation.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall();
        EasyMock.replay(processor);

        DefaultOperationPolicyIntrospector introspector = new DefaultOperationPolicyIntrospector(processor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestService.class, mapping);
        ServiceContract contract = contractProcessor.introspect(TestService.class, context);
        introspector.introspectPolicyOnOperations(contract, TestServiceImpl.class, context);
        EasyMock.verify(processor);
    }

    private static interface TestService {
        void test();

        void test(String string);
    }

    private static class TestServiceImpl implements TestService {

        public void test() {

        }

        @RolesAllowed("role1")
        public void test(String string) {

        }
    }
}
