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

import java.net.URI;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.implementation.mock.model.ImplementationMock;
import org.fabric3.implementation.mock.model.MockPhysicalComponent;
import org.fabric3.implementation.mock.provision.MockWireSource;
import org.fabric3.implementation.mock.provision.MockWireTarget;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class MockComponentGenerator implements ComponentGenerator<LogicalComponent<ImplementationMock>> {

    public MockPhysicalComponent generate(LogicalComponent<ImplementationMock> component) {
        MockPhysicalComponent physicalComponent = new MockPhysicalComponent();
        ImplementationMock implementationMock = component.getDefinition().getImplementation();
        physicalComponent.setInterfaces(implementationMock.getMockedInterfaces());
        return physicalComponent;
    }

    public MockWireTarget generateTarget(LogicalService service) {
        URI uri = service.getUri();
        ServiceContract serviceContract = service.getDefinition().getServiceContract();
        Class<?> interfaze = serviceContract.getInterfaceClass();
        return new MockWireTarget(uri, interfaze);
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) {
        throw new UnsupportedOperationException("Mock objects cannot have resources");
    }

    public PhysicalWireSource generateSource(LogicalReference reference) {
        throw new UnsupportedOperationException("Mock objects cannot be the source of a wire");
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSource generateCallbackSource(LogicalService service) {
        return new MockWireSource();
    }

}
