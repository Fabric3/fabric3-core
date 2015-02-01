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
package org.fabric3.spi.container.builder;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Creates wires between two components and between a component and a binding. Also handles disconnecting wires.
 */
public interface Connector {

    /**
     * Creates a wire for a components and connects it to another component or binding. In the case of bindings, the wire may be connected to a
     * binding source (for bound services) or to a binding target (for bound references).
     *
     * @param definition metadata describing the wire to create
     * @throws Fabric3Exception if an error creating the wire occurs
     */
    void connect(PhysicalWireDefinition definition) throws Fabric3Exception;


    /**
     * Disconnects a wire between two components or a component and a binding.
     *
     * @param definition the metadata describing the wire to disconnect
     * @throws Fabric3Exception if an error disconnecting the wire occurs
     */
    void disconnect(PhysicalWireDefinition definition) throws Fabric3Exception;
}
