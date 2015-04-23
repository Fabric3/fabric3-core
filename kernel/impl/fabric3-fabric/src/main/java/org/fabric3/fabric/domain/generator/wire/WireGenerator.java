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
 * Generates physical wires from logical wires, bound references and bound services.
 *
 * For unidirectional bound references and services, one physical wire will be generated. For bidirectional bound references and services (i.e. those with
 * callbacks), two physical wires will be generated.
 */
public interface WireGenerator {

    /**
     * Generates a {@link PhysicalWire} from a transport to a component.
     *
     * @param binding     the service binding
     * @param callbackUri the callback URI associated with this wire or null if the service is unidirectional
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateService(LogicalBinding<T> binding, URI callbackUri) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWire} from a component to the callback transport.
     *
     * @param binding the callback service binding
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateServiceCallback(LogicalBinding<T> binding) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWire} from a component reference to a transport.
     *
     * @param binding the reference binding
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateReference(LogicalBinding<T> binding) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWire} from a transport to a callback component
     *
     * @param binding the callback binding
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends Binding> PhysicalWire generateReferenceCallback(LogicalBinding<T> binding) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWire} for a wire.
     *
     * @param wire the logical wire
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    PhysicalWire generateWire(LogicalWire wire) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWire} for a callback wire.
     *
     * @param wire the logical wire
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    PhysicalWire generateWireCallback(LogicalWire wire) throws Fabric3Exception;

    /**
     * Generates a {@link PhysicalWire} for a resource.
     *
     * @param resourceReference the resource
     * @return the physical wire
     * @throws Fabric3Exception if an error occurs during generation
     */
    <T extends ResourceReference> PhysicalWire generateResource(LogicalResourceReference<T> resourceReference) throws Fabric3Exception;

}
