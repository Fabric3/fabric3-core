/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
