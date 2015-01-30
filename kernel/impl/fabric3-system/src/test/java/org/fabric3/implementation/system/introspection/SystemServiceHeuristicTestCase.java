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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.system.introspection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionException;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class SystemServiceHeuristicTestCase extends TestCase {
    private static final Set<Class<?>> NOCLASSES = Collections.emptySet();
    private SystemServiceHeuristic heuristic;

    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;
    private IntrospectionContext context;
    private InjectingComponentType componentType;
    private JavaServiceContract serviceInterfaceContract;
    private JavaServiceContract noInterfaceContract;
    private IMocksControl control;

    public void testNoInterface() throws IntrospectionException {
        EasyMock.expect(helper.getImplementedInterfaces(NoInterface.class)).andReturn(NOCLASSES);
        IntrospectionContext context = new DefaultIntrospectionContext();
        EasyMock.expect(contractProcessor.introspect(NoInterface.class, context, componentType)).andReturn(noInterfaceContract);
        control.replay();

        heuristic.applyHeuristics(componentType, NoInterface.class, context);
        Map<String, Service<ComponentType>> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(noInterfaceContract, services.get("NoInterface").getServiceContract());
        control.verify();
    }

    public void testWithInterface() throws IntrospectionException {
        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.add(ServiceInterface.class);

        IntrospectionContext context = new DefaultIntrospectionContext();
        EasyMock.expect(helper.getImplementedInterfaces(OneInterface.class)).andReturn(interfaces);
        EasyMock.expect(contractProcessor.introspect(ServiceInterface.class, context, componentType)).andReturn(serviceInterfaceContract);
        control.replay();

        heuristic.applyHeuristics(componentType, OneInterface.class, context);
        Map<String, Service<ComponentType>> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(serviceInterfaceContract, services.get("ServiceInterface").getServiceContract());
        control.verify();
    }

    public void testServiceWithExistingServices() throws IntrospectionException {
        Service<ComponentType> definition = new Service<>("Contract");
        componentType.add(definition);
        control.replay();

        heuristic.applyHeuristics(componentType, NoInterface.class, context);
        Map<String, Service<ComponentType>> services = componentType.getServices();
        assertEquals(1, services.size());
        assertSame(definition, services.get("Contract"));
        control.verify();
    }

    public static interface ServiceInterface {
    }

    public static class NoInterface {
    }

    public static class OneInterface implements ServiceInterface {
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        componentType = new InjectingComponentType(NoInterface.class.getName());

        noInterfaceContract = createServiceContract(NoInterface.class);
        serviceInterfaceContract = createServiceContract(ServiceInterface.class);

        control = EasyMock.createControl();
        context = control.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTypeMapping(EasyMock.isA(Class.class))).andStubReturn(null);
        contractProcessor = control.createMock(JavaContractProcessor.class);
        helper = control.createMock(IntrospectionHelper.class);
        heuristic = new SystemServiceHeuristic(contractProcessor, helper);
    }

    private JavaServiceContract createServiceContract(Class<?> type) {
        @SuppressWarnings("unchecked") JavaServiceContract contract = EasyMock.createMock(JavaServiceContract.class);
        EasyMock.expect(contract.getInterfaceName()).andStubReturn(type.getSimpleName());
        contract.setParent(EasyMock.isA(ModelObject.class));
        EasyMock.replay(contract);
        return contract;
    }
}