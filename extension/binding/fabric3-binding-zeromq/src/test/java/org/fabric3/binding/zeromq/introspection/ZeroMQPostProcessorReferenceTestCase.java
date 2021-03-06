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
package org.fabric3.binding.zeromq.introspection;

import junit.framework.TestCase;
import org.fabric3.api.binding.zeromq.annotation.ZeroMQ;
import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class ZeroMQPostProcessorReferenceTestCase extends TestCase {
    private ZeroMQPostProcessor processor = new ZeroMQPostProcessor();

    public void testServiceBinding() throws Exception {
        InjectingComponentType type = new InjectingComponentType(ServiceClientImpl.class);

        ServiceContract contract = new JavaServiceContract(Service.class);

        Reference<ComponentType> fieldReference = new Reference<>("fieldService", contract);
        FieldInjectionSite fieldSite = new FieldInjectionSite(ServiceClientImpl.class.getDeclaredField("fieldService"));
        type.add(fieldReference, fieldSite);

        Reference<ComponentType> methodReference = new Reference<>("methodService", contract);
        MethodInjectionSite methodSite = new MethodInjectionSite(ServiceClientImpl.class.getDeclaredMethod("setMethodService", Service.class), 0);
        type.add(methodReference, methodSite);

        Reference<ComponentType> ctorReference = new Reference<>("methodService", contract);
        ConstructorInjectionSite ctorSite = new ConstructorInjectionSite(ServiceClientImpl.class.getDeclaredConstructor(Service.class), 0);
        type.add(ctorReference, ctorSite);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, ServiceClientImpl.class, context);

        assertFalse(context.hasErrors());
        assertFalse(fieldReference.getBindings().isEmpty());

        ZeroMQBinding fieldBinding = (ZeroMQBinding) fieldReference.getBindings().get(0);
        assertEquals(2, fieldBinding.getZeroMQMetadata().getSocketAddresses().size());

        assertFalse(methodReference.getBindings().isEmpty());
        ZeroMQBinding methodBinding = (ZeroMQBinding) methodReference.getBindings().get(0);
        assertEquals("Service", methodBinding.getTargetUri().toString());
        assertFalse(ctorReference.getBindings().isEmpty());
    }

    private interface Service {

    }

    private static class ServiceClientImpl {

        @ZeroMQ(addresses = "123.3.3:80 123.3.4:80")
        @org.oasisopen.sca.annotation.Reference
        protected Service fieldService;

        private Service service;

        @ZeroMQ(target = "Service")
        @org.oasisopen.sca.annotation.Reference
        public void setMethodService(Service service) {
            this.service = service;
        }

        public ServiceClientImpl(@org.oasisopen.sca.annotation.Reference @ZeroMQ Service service) {
            this.service = service;
        }
    }

}
