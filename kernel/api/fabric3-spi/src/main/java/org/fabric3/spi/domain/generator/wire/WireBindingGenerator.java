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
package org.fabric3.spi.domain.generator.wire;

import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Generates {@link PhysicalWireSourceDefinition}s and {@link PhysicalWireTargetDefinition}s for resolved wire bindings.
 */
public interface WireBindingGenerator<BD extends Binding> {

    /**
     * Generates metadata used to attach a physical wire connected to a target service to a source transport.
     *
     * @param serviceBinding the binding specified on the service
     * @param contract       the service contract
     * @param operations     the operations to generate the wire for
     * @return Physical wire source definition.
     * @throws ContainerException if an error is raised during generation
     */
    PhysicalWireSourceDefinition generateSource(LogicalBinding<BD> serviceBinding, ServiceContract contract, List<LogicalOperation> operations)
            throws ContainerException;

    /**
     * Generates metadata used to attach a physical wire connected to a source component to a target transport. This method is called when a reference is
     * configured with a binding.
     *
     * @param referenceBinding the binding specified on the reference
     * @param contract         the service contract
     * @param operations       the operations to generate the wire for
     * @return Physical wire target definition.
     * @throws ContainerException if an error is raised during generation
     */
    PhysicalWireTargetDefinition generateTarget(LogicalBinding<BD> referenceBinding, ServiceContract contract, List<LogicalOperation> operations)
            throws ContainerException;

    /**
     * Generates metadata used to attach a physical wire connected to a source component to a target transport. This method is called when the reference is
     * wired using the <code>@target</code> attribute of the <code>&lt;reference&gt;</code> element. In this case, the reference is wired without a binding to a
     * service hosted in the same domain and the target service binding configuration is used to calculate the physical wire for the reference.
     *
     * @param serviceBinding the binding specified on the service
     * @param contract       the service contract
     * @param operations     the operations to generate the wire for
     * @return Physical wire target definition.
     * @throws ContainerException if an error is raised during generation
     */
    PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<BD> serviceBinding, ServiceContract contract, List<LogicalOperation> operations)
            throws ContainerException;

}
