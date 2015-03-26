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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.mock.introspection;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class MockPhysicalComponentTypeLoaderImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testLoad() throws Exception {

        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);


        JavaContractProcessor processor = EasyMock.createMock(JavaContractProcessor.class);
        JavaServiceContract controlContract = new JavaServiceContract(IMocksControl.class);
        JavaServiceContract fooContract = new JavaServiceContract(Foo.class);
        EasyMock.expect(processor.introspect(
                EasyMock.eq(IMocksControl.class),
                EasyMock.isA(IntrospectionContext.class))).andReturn(controlContract);
        EasyMock.expect(processor.introspect(
                EasyMock.eq(Foo.class),
                EasyMock.isA(IntrospectionContext.class),
                EasyMock.isA(InjectingComponentType.class))).andReturn(fooContract);
        EasyMock.replay(processor);

        MockComponentTypeLoader componentTypeLoader = new MockComponentTypeLoaderImpl(processor);

        List<String> mockedInterfaces = new LinkedList<>();
        mockedInterfaces.add(Foo.class.getName());

        InjectingComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);

        assertNotNull(componentType);
        java.util.Map<String, Service<ComponentType>> services = componentType.getServices();

        assertEquals(2, services.size());    // 4 because the mock service is added implicitly

        Service<ComponentType> service = services.get("Foo");
        assertNotNull(service);
        assertEquals(Foo.class.getName(), service.getServiceContract().getQualifiedInterfaceName());


    }

}
