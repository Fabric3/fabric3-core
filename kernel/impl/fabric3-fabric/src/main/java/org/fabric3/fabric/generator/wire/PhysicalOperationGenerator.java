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
package org.fabric3.fabric.generator.wire;

import java.util.List;
import java.util.Set;

import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * Generates PhysicalOperationDefinitions which are used to instantiate interceptor chains for a wire, bound service, or bound reference on a
 * runtime.
 */
public interface PhysicalOperationGenerator {

    /**
     * Generates a PhysicalOperationDefinition when the source reference and target service contracts are the same.
     *
     * @param operations the logical operations to generate from
     * @param remote     true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process
     *                   wire.
     * @param result     resolved policy metadata
     * @return the PhysicalOperationDefinition
     * @throws GenerationException if there is an error generating the operations
     */
    Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> operations, boolean remote, PolicyResult result)
            throws GenerationException;


    /**
     * Generates a PhysicalOperationDefinition when the source reference and target service contracts are different.
     *
     * @param sources the source logical operations to generate from
     * @param targets the target logical operations to generate from
     * @param remote  true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process
     *                wire.
     * @param result  resolved policy metadata
     * @return the PhysicalOperationDefinition
     * @throws GenerationException if there is an error generating the operations
     */
    Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> sources,
                                                        List<LogicalOperation> targets,
                                                        boolean remote,
                                                        PolicyResult result) throws GenerationException;

}