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
 */
package org.fabric3.binding.ws.metro.generator;

import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;

/**
 * Generates source and target definitions for a service contract subtype.
 */
public interface MetroGeneratorDelegate<T extends ServiceContract> {

    /**
     * Generates a source definition from a logical binding.
     *
     * @param serviceBinding logical binding.
     * @param contract       the service contract
     * @return Physical wire source definition.
     * @throws Fabric3Exception if an error is raised during generation
     */
    MetroWireSourceDefinition generateSource(LogicalBinding<WsBinding> serviceBinding, T contract) throws Fabric3Exception;

    /**
     * Generates a target definition from a logical binding.
     *
     * @param referenceBinding logical binding.
     * @param contract         the service contract
     * @return Physical wire target definition.
     * @throws Fabric3Exception if an error is raised during generation
     */
    MetroWireTargetDefinition generateTarget(LogicalBinding<WsBinding> referenceBinding, T contract) throws Fabric3Exception;

    /**
     * Generates a target definition from logical reference and service bindings.
     *
     * @param serviceBinding logical service binding.
     * @param contract       the service contract
     * @return Physical wire target definition.
     * @throws Fabric3Exception if an error is raised during generation
     */
    MetroWireTargetDefinition generateServiceBindingTarget(LogicalBinding<WsBinding> serviceBinding, T contract) throws Fabric3Exception;

}
