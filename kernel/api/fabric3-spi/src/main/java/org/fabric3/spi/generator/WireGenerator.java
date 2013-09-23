/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.spi.generator;

import java.net.URI;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates physical wire definitions from logical wires, bound references and bound services. The methods correspond to how the physical wire
 * generation scheme works, which is described below:
 * <p/>
 * For unidirectional bound references and services, one physical wire will be generated. For bidirectional bound references and services (i.e. those
 * with callbacks), two physical wires will be generated.
 * <p/>
 * The number of physical wires generated from a logical wire will vary depending on whether the target service is collocated or remote. A
 * unidirectional wire to a collocated service will generate one physical wire. A bidirectional wire (i.e. with a callback) to a collocated service
 * will generate two physical wires. A unidirecitonal wire to a remote service offered by a component will generate two physical wires:
 * <pre>
 * <ul>
 * <li>One from the source reference to the transport
 * <li>One from the transport on the target runtime to the target service.
 * </ul>
 * </pre>
 * A bidirectional wire to a remote service offered by a component will generate four wires:
 * <pre>
 * <ul>
 * <li>One from the source reference to the transport
 * <li>One from the transport on the target runtime to the target service.
 * <li>One from the callback site on the target to the transport
 * <li>One from the transport on the source runtime to the callback service.
 * </ul>
 * </pre>
 */
public interface WireGenerator {

    /**
     * Generates a PhysicalWireDefinition for a bound service.
     *
     * @param binding     the service binding
     * @param callbackUri the callback URI associated with this wire or null if the service is unidirectional
     * @return the physical wire definition.
     * @throws GenerationException if an error occurs during generation
     */
    <T extends BindingDefinition> PhysicalWireDefinition generateBoundService(LogicalBinding<T> binding, URI callbackUri)  throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for callback wire from a component to the callback service provided by a forward service
     *
     * @param binding the callback service binding
     * @return the physical wire definition.
     * @throws GenerationException if an error occurs during generation
     */
    <T extends BindingDefinition> PhysicalWireDefinition generateBoundServiceCallback(LogicalBinding<T> binding) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for a bound reference.
     *
     * @param binding the reference binding
     * @return the physical wire definition.
     * @throws GenerationException if an error occurs during generation
     */
    <T extends BindingDefinition> PhysicalWireDefinition generateBoundReference(LogicalBinding<T> binding) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for callback wire for a bound reference
     *
     * @param binding the callback binding
     * @return the physical wire definition.
     * @throws GenerationException if an error occurs during generation
     */
    <T extends BindingDefinition> PhysicalWireDefinition generateBoundReferenceCallback(LogicalBinding<T> binding) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for a wire between collocated components.
     *
     * @param wire the logical wire
     * @return the physical wire definition.
     * @throws GenerationException if an error occurs during generation
     */
    PhysicalWireDefinition generateWire(LogicalWire wire) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for a callback wire between collocated components.
     *
     * @param wire the logical wire
     * @return the physical wire definition.
     * @throws GenerationException if an error occurs during generation
     */
    PhysicalWireDefinition generateWireCallback(LogicalWire wire) throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for the resource.
     *
     * @param resourceReference the resource
     * @return the physical wire definition
     * @throws GenerationException if an error occurs during generation
     */
    <T extends ResourceReferenceDefinition> PhysicalWireDefinition generateResource(LogicalResourceReference<T> resourceReference) throws GenerationException;


}
