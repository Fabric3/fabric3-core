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
package org.fabric3.spi.container.builder.component;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Attaches and detaches a wire to/from a target component or transport binding.
 */
public interface TargetWireAttacher<PTD extends PhysicalWireTargetDefinition> {
    /**
     * Attaches a wire to a target component or outgoing transport binding.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @param wire   the wire
     * @throws ContainerException if an exception occurs during the attach operation
     */
    void attach(PhysicalWireSourceDefinition source, PTD target, Wire wire) throws ContainerException;

    /**
     * Detaches a wire from a target component or outgoing transport binding.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @throws ContainerException if an exception occurs during the detach operation
     */
    void detach(PhysicalWireSourceDefinition source, PTD target) throws ContainerException;

    /**
     * Create an ObjectFactory that returns a direct target instance.
     *
     * @param target metadata for performing the attach
     * @return an ObjectFactory that returns the target instance
     * @throws ContainerException if an exception occurs during the attach operation
     */
    ObjectFactory<?> createObjectFactory(PTD target) throws ContainerException;

}
