/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.generator;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Generates metadata used to provision components and physical wires to runtimes.
 *
 * @version $Rev$ $Date$
 */
public interface ComponentGenerator<C extends LogicalComponent<? extends Implementation<?>>> {

    /**
     * Generates an {@link org.fabric3.spi.model.physical.PhysicalComponentDefinition} based on a {@link ComponentDefinition}. The resulting
     * PhysicalComponentDefinition is added to the PhysicalChangeSet associated with the current GeneratorContext.
     *
     * @param component the logical component to evaluate
     * @return the physical component definition
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalComponentDefinition generate(C component) throws GenerationException;

    /**
     * Generates a {@link PhysicalSourceDefinition} used to attach a physical wire to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a runtime.
     *
     * @param reference the source logical reference
     * @param policy    the provided intents and policy sets
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException;

    /**
     * Generates a {@link PhysicalTargetDefinition} used to attach a physical wire to a target component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its target on a runtime.
     *
     * @param service the target logical service
     * @param policy  the provided intents and policy sets
     * @return the metadata used to attach the wire to its target on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException;

    /**
     * Generates a {@link PhysicalSourceDefinition} used to attach a physical wire for a callback service to a source component. Metadata contained in
     * the PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a
     * runtime.
     *
     * @param service the forward service the callback is being generated for
     * @param policy  the provided intents and policy sets
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException;

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
     * Generates a {@link PhysicalSourceDefinition} used to attach a physical resource to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a runtime.
     *
     * @param resourceReference the source logical resource
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException;

}
