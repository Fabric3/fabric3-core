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
package org.fabric3.binding.zeromq.introspection;

import junit.framework.TestCase;
import org.fabric3.api.binding.zeromq.annotation.ZeroMQ;
import org.fabric3.api.model.type.component.ServiceDefinition;
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
        ServiceDefinition serviceDefinition = addService(Service.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, SingleService.class, context);

        assertFalse(context.hasErrors());
        assertFalse(serviceDefinition.getBindings().isEmpty());
    }

    public void testMultiServiceBinding() throws Exception {
        InjectingComponentType type = new InjectingComponentType(MultiService.class.getName());
        ServiceDefinition serviceDefinition = addService(Service.class, type);
        ServiceDefinition serviceDefinition2 = addService(Service2.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, MultiService.class, context);

        assertFalse(context.hasErrors());
        assertTrue(serviceDefinition.getBindings().isEmpty());
        assertFalse(serviceDefinition2.getBindings().isEmpty());
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
        ServiceDefinition serviceDefinition = addService(BiDirectionalService.class, type);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, BiDirectionalServiceImpl.class, context);

        assertFalse(context.hasErrors());
        assertFalse(serviceDefinition.getBindings().isEmpty());
    }

    private ServiceDefinition addService(Class<?> interfaze, InjectingComponentType type) {
        ServiceDefinition serviceDefinition = new ServiceDefinition(interfaze.getSimpleName());
        serviceDefinition.setServiceContract(new JavaServiceContract(interfaze));
        type.add(serviceDefinition);
        return serviceDefinition;
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
