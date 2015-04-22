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
package org.fabric3.fabric.domain.generator.wire;

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWire;

/**
 * Generates physical wire definitions from logical wires, bound references and bound services. The methods correspond to how the physical wire generation
 * scheme works, which is described below:
 *
 * For unidirectional bound references and services, one physical wire will be generated. For bidirectional bound references and services (i.e. those with
 * callbacks), two physical wires will be generated.
 *
 * The number of physical wires generated from a logical wire will vary depending on whether the target service is collocated or remote. A unidirectional wire
 * to a collocated service will generate one physical wire. A bidirectional wire (i.e. with a callback) to a collocated service will generate two physical
 * wires. A unidirecitonal wire to a remote service offered by a component will generate two physical wires:
 *
 * - One from the source reference to the transport
 *
 * - One from the transport on the target runtime to the target service.
 *
 * A bidirectional wire to a remote service offered by a component will generate four wires:
 *
 * - One from the source reference to the transport
 *
 * - One from the transport on the target runtime to the target service.
 *
 * - One from the callback site on the target to the transport
 *
 * - One from the transport on the source runtime to the callback service.
 */
public interface WireGenerator {

    /**
     * Generates a PhysicalWireDefinition for a bound service.
     *
     * @param binding     the service binding
     * @param callbackUri the callback URI associated with this wire or null if the service is unidirectional
     * @return the physical wire definition.
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateBoundService(LogicalBinding<T> binding, URI callbackUri) throws Fabric3Exception;

    /**
     * Generates a PhysicalWireDefinition for callback wire from a component to the callback service provided by a forward service
     *
     * @param binding the callback service binding
     * @return the physical wire definition.
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateBoundServiceCallback(LogicalBinding<T> binding) throws Fabric3Exception;

    /**
     * Generates a PhysicalWireDefinition for a bound reference.
     *
     * @param binding the reference binding
     * @return the physical wire definition.
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateBoundReference(LogicalBinding<T> binding) throws Fabric3Exception;

    /**
     * Generates a PhysicalWireDefinition for callback wire for a bound reference
     *
     * @param binding the callback binding
     * @return the physical wire definition.
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateBoundReferenceCallback(LogicalBinding<T> binding) throws Fabric3Exception;

    /**
     * Generates a PhysicalWireDefinition for a wire.
     *
     * @param wire the logical wire
     * @return the physical wire definition.
     * @throws Fabric3Exception if an error occurs during generation
     */
    PhysicalWire generateWire(LogicalWire wire) throws Fabric3Exception;

    /**
     * Generates a PhysicalWireDefinition for a callback wire.
     *
     * @param wire the logical wire
     * @return the physical wire definition.
     * @throws Fabric3Exception if an error occurs during generation
     */
    PhysicalWire generateWireCallback(LogicalWire wire) throws Fabric3Exception;

    /**
     * Generates a PhysicalWireDefinition for the resource.
     *
     * @param resourceReference the resource
     * @return the physical wire definition
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends ResourceReference> PhysicalWire generateResource(LogicalResourceReference<T> resourceReference) throws Fabric3Exception;

}
