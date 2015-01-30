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
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Callback;

/**
 *
 */
public class ZeroMQPostProcessorServiceTestCase extends TestCase {
    private ZeroMQPostProcessor processor = new ZeroMQPostProcessor();

    public void testServiceBinding() throws Exception {
        InjectingComponentType type = new InjectingComponentType(SingleService.class.getName());
        org.fabric3.api.model.type.component.Service service = addService(Service.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, SingleService.class, context);

        assertFalse(context.hasErrors());
        assertFalse(service.getBindings().isEmpty());
    }

    public void testMultiServiceBinding() throws Exception {
        InjectingComponentType type = new InjectingComponentType(MultiService.class.getName());
        org.fabric3.api.model.type.component.Service service = addService(Service.class, type);
        org.fabric3.api.model.type.component.Service service2 = addService(Service2.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, MultiService.class, context);

        assertFalse(context.hasErrors());
        assertTrue(service.getBindings().isEmpty());
        assertFalse(service2.getBindings().isEmpty());
    }

    public void testMultiNoService() throws Exception {
        InjectingComponentType type = new InjectingComponentType(MultiNoServiceSpecified.class.getName());
        addService(Service.class, type);
        addService(Service2.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, MultiNoServiceSpecified.class, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidAnnotation);
    }

    public void testWrongServiceSpecified() throws Exception {
        InjectingComponentType type = new InjectingComponentType(WrongServiceSpecified.class.getName());
        addService(Service.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, WrongServiceSpecified.class, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidAnnotation);
    }

    public void testBiDirectionalServiceBinding() throws Exception {
        InjectingComponentType type = new InjectingComponentType(BiDirectionalServiceImpl.class.getName());
        org.fabric3.api.model.type.component.Service service = addService(BiDirectionalService.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, BiDirectionalServiceImpl.class, context);

        assertFalse(context.hasErrors());
        assertFalse(service.getBindings().isEmpty());
    }

    private org.fabric3.api.model.type.component.Service addService(Class<?> interfaze, InjectingComponentType type) {
        org.fabric3.api.model.type.component.Service service = new org.fabric3.api.model.type.component.Service(interfaze.getSimpleName());
        service.setServiceContract(new JavaServiceContract(interfaze));
        type.add(service);
        return service;
    }

    private interface Service {

    }

    private interface Service2 {

    }

    @Callback(CallbackService.class)
    private interface BiDirectionalService {

    }

    private interface CallbackService {

    }

    @ZeroMQ
    private class SingleService implements Service {

    }

    @ZeroMQ(service = Service2.class)
    private class MultiService implements Service, Service2 {

    }

    @ZeroMQ
    private class MultiNoServiceSpecified implements Service, Service2 {

    }

    @ZeroMQ(service = Service2.class)
    private class WrongServiceSpecified implements Service {

    }

    @ZeroMQ
    private class BiDirectionalServiceImpl implements BiDirectionalService {

    }
}
