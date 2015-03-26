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

import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalOperation;

/**
 * Generates PhysicalOperationDefinitions which are used to instantiate interceptor chains for a wire, bound service, or bound reference on a runtime.
 */
public interface PhysicalOperationGenerator {

    /**
     * @param operations the logical operations to generate from
     * @return the PhysicalOperationDefinition
     * @throws Fabric3Exception if there is an error generating the operations
     */
    Set<PhysicalOperation> generateOperations(List<LogicalOperation> operations) throws Fabric3Exception;

    /**
     * Generates a PhysicalOperationDefinition when the source reference and target service contracts are different.
     *
     * @param sources the source logical operations to generate from
     * @param targets the target logical operations to generate from
     * @param remote  true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process wire.
     * @return the PhysicalOperationDefinition
     * @throws Fabric3Exception if there is an error generating the operations
     */
    Set<PhysicalOperation> generateOperations(List<LogicalOperation> sources, List<LogicalOperation> targets, boolean remote)
            throws Fabric3Exception;

}