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
import org.fabric3.api.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ZeroMQPostProcessorReferenceTestCase extends TestCase {
    private ZeroMQPostProcessor processor = new ZeroMQPostProcessor();

    public void testServiceBinding() throws Exception {
        InjectingComponentType type = new InjectingComponentType(ServiceClientImpl.class.getName());

        ServiceContract contract = new JavaServiceContract(Service.class);

        ReferenceDefinition fieldReference = new ReferenceDefinition("fieldService", contract);
        FieldInjectionSite fieldSite = new FieldInjectionSite(ServiceClientImpl.class.getDeclaredField("fieldService"));
        type.add(fieldReference, fieldSite);

        ReferenceDefinition methodReference = new ReferenceDefinition("methodService", contract);
        MethodInjectionSite methodSite = new MethodInjectionSite(ServiceClientImpl.class.getDeclaredMethod("setMethodService", Service.class), 0);
        type.add(methodReference, methodSite);

        ReferenceDefinition ctorReference = new ReferenceDefinition("methodService", contract);
        ConstructorInjectionSite ctorSite = new ConstructorInjectionSite(ServiceClientImpl.class.getDeclaredConstructor(Service.class), 0);
        type.add(ctorReference, ctorSite);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(type, ServiceClientImpl.class, context);

        assertFalse(context.hasErrors());
        assertFalse(fieldReference.getBindings().isEmpty());

        ZeroMQBindingDefinition fieldBinding = (ZeroMQBindingDefinition) fieldReference.getBindings().get(0);
        assertEquals(2, fieldBinding.getZeroMQMetadata().getSocketAddresses().size());

        assertFalse(methodReference.getBindings().isEmpty());
        ZeroMQBindingDefinition methodBinding = (ZeroMQBindingDefinition) methodReference.getBindings().get(0);
        assertEquals("Service", methodBinding.getTargetUri().toString());
        assertFalse(ctorReference.getBindings().isEmpty());
    }

    private interface Service {

    }

    private static class ServiceClientImpl {

        @ZeroMQ(addresses = "123.3.3:80 123.3.4:80")
        @Reference
        protected Service fieldService;

        private Service service;

        @ZeroMQ(target = "Service")
        @Reference
        public void setMethodService(Service service) {
            this.service = service;
        }

        public ServiceClientImpl(@Reference @ZeroMQ Service service) {
            this.service = service;
        }
    }

}
