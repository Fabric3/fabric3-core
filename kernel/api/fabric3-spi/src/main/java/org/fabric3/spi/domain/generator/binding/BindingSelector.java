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
package org.fabric3.spi.domain.generator.binding;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Responsible for selecting and configuring binding configuration for wires and channels in a domain.
 */
public interface BindingSelector {

    /**
     * Selects and configures bindings in a domain.
     *
     * @param domain the domain component
     * @throws ContainerException if an error occurs selecting a binding
     */
    void selectBindings(LogicalCompositeComponent domain) throws ContainerException;

    /**
     * Selects and configures a binding for a wire.
     *
     * @param wire the wire
     * @throws ContainerException if an error occurs selecting a binding
     */
    void selectBinding(LogicalWire wire) throws ContainerException;

}
