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
package org.fabric3.spi.domain.generator.component;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Generates metadata used to provision components and physical wires to a runtime.
 */
public interface ComponentGenerator<C extends LogicalComponent<? extends Implementation<?>>> {

    /**
     * Generates an {@link PhysicalComponentDefinition} based on a {@link ComponentDefinition}. The resulting
     * PhysicalComponentDefinition is added to the PhysicalChangeSet associated with the current GeneratorContext.
     *
     * @param component the logical component to evaluate
     * @return the physical component definition
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalComponentDefinition generate(C component) throws GenerationException;

    /**
     * Generates a {@link PhysicalWireSourceDefinition} used to attach a physical wire to a source component. Metadata contained in the PhysicalWireSourceDefinition
     * is specific to the component implementation type and used when the wire is attached to its source on a runtime.
     *
     * @param reference the source logical reference
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalWireSourceDefinition generateSource(LogicalReference reference) throws GenerationException;

    /**
     * Generates a {@link PhysicalWireTargetDefinition} used to attach a physical wire to a target component. Metadata contained in the PhysicalWireSourceDefinition
     * is specific to the component implementation type and used when the wire is attached to its target on a runtime.
     *
     * @param service the target logical service
     * @return the metadata used to attach the wire to its target on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalWireTargetDefinition generateTarget(LogicalService service) throws GenerationException;

    /**
     * Generates a {@link PhysicalWireSourceDefinition} used to attach a physical wire for a callback service to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a runtime.
     *
     * @param service the forward service the callback is being generated for
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalWireSourceDefinition generateCallbackSource(LogicalService service) throws GenerationException;

    /**
     * Generates a {@link PhysicalConnectionSourceDefinition} used to attach an event connection to its source producer.
     *
     * @param producer the producer
     * @return the connection metadata
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException;

    /**
     * Generates a {@link PhysicalConnectionTargetDefinition} used to attach an event connection to its target consumer.
     *
     * @param consumer the consumer
     * @return the connection metadata
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException;

    /**
     * Generates a {@link PhysicalWireSourceDefinition} used to attach a physical resource to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a runtime.
     *
     * @param resourceReference the source logical resource
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalWireSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException;

}
