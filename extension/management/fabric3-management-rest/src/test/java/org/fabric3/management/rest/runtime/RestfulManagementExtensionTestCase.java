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
package org.fabric3.management.rest.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.management.rest.transformer.TransformerPair;
import org.fabric3.management.rest.transformer.TransformerPairService;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.api.model.type.java.ManagementOperationInfo;
import org.fabric3.api.model.type.java.OperationType;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;

/**
 *
 */
public class RestfulManagementExtensionTestCase extends TestCase {
    private TransformerPairService pairService;
    private Marshaller marshaller;
    private ResourceHost host;
    private RestfulManagementExtension extension;
    private ClassLoader loader;

    public void testExportComponent() throws Exception {
        String path = "services/service";
        EasyMock.expect(host.isPathRegistered("services", Verb.GET)).andReturn(false).atLeastOnce();
        EasyMock.expect(host.isPathRegistered(path, Verb.GET)).andReturn(false).atLeastOnce();
        host.register(EasyMock.isA(ResourceMapping.class));
        // a dynamic resource for services, a resource for services/service, and a resource for services/service/operation must be registered
        EasyMock.expectLastCall().times(3);
        EasyMock.replay(pairService, marshaller, host);

        Set<Role> readRoles = Collections.singleton(new Role("supervisor"));
        Set<Role> writeRoles = Collections.singleton(new Role("admin"));
        String clazz = TestComponent.class.getName();

        ManagementInfo info = new ManagementInfo("component", "group", path, "description", clazz, readRoles, writeRoles);
        Method method = TestComponent.class.getMethod("setOperation", String.class);
        Signature signature = new Signature(method);
        ManagementOperationInfo operation = new ManagementOperationInfo(signature, "operation", OperationType.POST, "description", writeRoles);
        info.addOperation(operation);

        ObjectFactory<?> factory = new SingletonObjectFactory<TestComponent>(new TestComponent());
        extension.export(URI.create("fabric3://domain/Component"), info, factory, loader);

        EasyMock.verify(pairService, marshaller, host);
    }


    public void testExportInstance() throws Exception {
        String path = "/runtime/services/service";     // note runtime is appended since instance exports are placed under /runtime
        EasyMock.expect(host.isPathRegistered("/runtime", Verb.GET)).andReturn(true).atLeastOnce();
        EasyMock.expect(host.isPathRegistered("/runtime/services", Verb.GET)).andReturn(false).atLeastOnce();
        EasyMock.expect(host.isPathRegistered(path, Verb.GET)).andReturn(false).atLeastOnce();
        host.register(EasyMock.isA(ResourceMapping.class));
        // a dynamic GET resource for services, a dynamic GET resource for services/service, a dynamic SET resource for services/service/operation, 
        // and a resource for GET operation must be registered,
        EasyMock.expectLastCall().times(4);

        EasyMock.replay(pairService, marshaller, host);

        TestComponent instance = new TestComponent();
        extension.export("services/service", "group", "description", instance);

        EasyMock.verify(pairService, marshaller, host);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pairService = EasyMock.createMock(TransformerPairService.class);
        TransformerPair pair = new TransformerPair(null, null);
        EasyMock.expect(pairService.getTransformerPair(EasyMock.isA(List.class),
                                                       EasyMock.isA(DataType.class),
                                                       EasyMock.isA(DataType.class))).andReturn(pair).atLeastOnce();
        marshaller = EasyMock.createMock(Marshaller.class);
        host = EasyMock.createMock(ResourceHost.class);
        loader = getClass().getClassLoader();

        extension = new RestfulManagementExtension(pairService, marshaller, host);
        extension.init();
    }

    @Management
    private class TestComponent {

        @ManagementOperation
        public void setOperation(String param) {
        }

        @ManagementOperation
        public void getOperation() {
        }

        public void nonManagementOperation() {
        }
    }
}
