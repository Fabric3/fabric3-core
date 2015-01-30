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
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.api.model.type.java.ManagementOperationInfo;
import org.fabric3.api.model.type.java.OperationType;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.management.rest.transformer.TransformerPair;
import org.fabric3.management.rest.transformer.TransformerPairService;
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

        ObjectFactory<?> factory = new SingletonObjectFactory<>(new TestComponent());
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
