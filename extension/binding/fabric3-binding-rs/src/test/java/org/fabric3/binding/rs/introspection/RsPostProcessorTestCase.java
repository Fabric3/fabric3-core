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
package org.fabric3.binding.rs.introspection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class RsPostProcessorTestCase extends TestCase {
    private RsPostProcessor processor = new RsPostProcessor();

    public void testIntrospectImplClass() throws Exception {
        InjectingComponentType type = new InjectingComponentType(TestImpl.class.getName());
        ServiceDefinition service = new ServiceDefinition("TestImpl", new JavaServiceContract(TestImpl.class));
        type.add(service);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, TestImpl.class, context);

        assertFalse(context.hasErrors());
        assertFalse(service.getBindings().isEmpty());
    }

    public void testIntrospectSingleService() throws Exception {
        InjectingComponentType type = new InjectingComponentType(TestSingleService.class.getName());
        ServiceDefinition service = new ServiceDefinition("TestService", new JavaServiceContract(TestService.class));
        type.add(service);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, TestSingleService.class, context);

        assertFalse(context.hasErrors());
        assertFalse(service.getBindings().isEmpty());
    }

    public void testIntrospectMultiService() throws Exception {
        InjectingComponentType type = new InjectingComponentType(TestMultiService.class.getName());
        ServiceDefinition service = new ServiceDefinition("TestService", new JavaServiceContract(TestService.class));
        type.add(service);
        ServiceDefinition service2 = new ServiceDefinition("TestService2", new JavaServiceContract(TestService2.class));
        type.add(service2);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, TestMultiService.class, context);

        assertFalse(context.hasErrors());
        assertFalse(service.getBindings().isEmpty());
        assertTrue(service2.getBindings().isEmpty());
    }

    @Path("/")
    private class TestImpl {

    }

    private interface TestService {

        String getMessage();
    }

    private interface TestService2 {

    }

    @Path("/")
    public class TestSingleService implements TestService {

        public String getMessage() {
            return "test";
        }
    }

    @Path("/")
    public class TestMultiService implements TestService, TestService2 {

        @GET
        public String getMessage() {
            return "test";
        }
    }
}
