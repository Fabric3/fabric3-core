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
package org.fabric3.spi.domain.generator;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Implementation;
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
 * Generates metadata used to provision components and physical wires to a runtime.
 */
public interface ComponentGenerator<C extends LogicalComponent<? extends Implementation<?>>> {

    /**
     * Generates an {@link PhysicalComponent} based on a {@link LogicalComponent}.
     *
     * @param component the logical component to evaluate
     * @return the physical component definition
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalComponent generate(C component) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWireSource} used to attach a physical wire to a source component. Metadata contained in the source is specific to the
     * component implementation type and used when the wire is attached to its source.
     *
     * @param reference the source logical reference
     * @return the metadata used to attach the wire to its source
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalWireSource generateSource(LogicalReference reference) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWireTarget} used to attach a physical wire to a target component. Metadata contained in the source is specific to the
     * component implementation type and used when the wire is attached to its target.
     *
     * @param service the target logical service
     * @return the metadata used to attach the wire to its target on the service node
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalWireTarget generateTarget(LogicalService service) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWireSource} used to attach a physical wire for a callback service to a source component. Metadata contained in the source is
     * specific to the component implementation type and used when the wire is attached to its source.
     *
     * @param service the forward service the callback is being generated for
     * @return the metadata used to attach the wire to its source on the service node
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalWireSource generateCallbackSource(LogicalService service) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalConnectionSource} used to attach an event connection to its source producer.
     *
     * @param producer the producer
     * @return the connection metadata
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalConnectionTarget} used to attach an event connection to its target consumer.
     *
     * @param consumer the consumer
     * @return the connection metadata
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWireSource} used to attach a physical resource to a source component. Metadata contained in the source is specific to the
     * component implementation type and used when the wire is attached to its source.
     *
     * @param resourceReference the source logical resource
     * @return the metadata used to attach the wire to its source on the service node
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) throws Fabric3Exception;

}
