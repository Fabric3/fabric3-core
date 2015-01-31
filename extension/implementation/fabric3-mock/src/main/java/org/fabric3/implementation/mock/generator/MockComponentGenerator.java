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
package org.fabric3.implementation.mock.generator;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.implementation.mock.model.ImplementationMock;
import org.fabric3.implementation.mock.model.MockComponentDefinition;
import org.fabric3.implementation.mock.provision.MockWireSourceDefinition;
import org.fabric3.implementation.mock.provision.MockWireTargetDefinition;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class MockComponentGenerator implements ComponentGenerator<LogicalComponent<ImplementationMock>> {

    public MockComponentDefinition generate(LogicalComponent<ImplementationMock> component) throws ContainerException {
        MockComponentDefinition componentDefinition = new MockComponentDefinition();
        ImplementationMock implementationMock = component.getDefinition().getImplementation();
        componentDefinition.setInterfaces(implementationMock.getMockedInterfaces());
        return componentDefinition;
    }

    public MockWireTargetDefinition generateTarget(LogicalService service) throws ContainerException {
        MockWireTargetDefinition definition = new MockWireTargetDefinition();
        definition.setUri(service.getUri());
        ServiceContract serviceContract = service.getDefinition().getServiceContract();
        definition.setMockedInterface(serviceContract.getQualifiedInterfaceName());
        return definition;
    }

    public PhysicalWireSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) {
        throw new UnsupportedOperationException("Mock objects cannot have resources");
    }

    public PhysicalWireSourceDefinition generateSource(LogicalReference reference) {
        throw new UnsupportedOperationException("Mock objects cannot be the source of a wire");
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws ContainerException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateCallbackSource(LogicalService service) throws ContainerException {
        return new MockWireSourceDefinition();
    }

}
