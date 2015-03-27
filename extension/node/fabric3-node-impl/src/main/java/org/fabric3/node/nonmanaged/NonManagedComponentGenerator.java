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
package org.fabric3.node.nonmanaged;

import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponent;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;

/**
 * Generates a physical source definition to connect wires to non-managed code.
 */
public class NonManagedComponentGenerator implements ComponentGenerator<LogicalComponent<NonManagedImplementation>> {

    public PhysicalWireSource generateSource(LogicalReference reference) {
        Class<?> interfaze = reference.getServiceContract().getInterfaceClass();
        return new NonManagedWireSource(interfaze);
    }

    public PhysicalComponent generate(LogicalComponent<NonManagedImplementation> component) {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTarget generateTarget(LogicalService service) {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSource generateCallbackSource(LogicalService service) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        Class<?> interfaze = producer.getServiceContract().getInterfaceClass();
        return new NonManagedConnectionSource(interfaze);
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) {
        Class<?> interfaze = consumer.getServiceContract().getInterfaceClass();
        return new NonManagedConnectionTarget(interfaze);
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) {
        throw new UnsupportedOperationException();
    }
}
