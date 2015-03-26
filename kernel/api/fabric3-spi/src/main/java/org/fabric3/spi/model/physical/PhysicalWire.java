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
package org.fabric3.spi.model.physical;

import java.util.Set;

/**
 * Used to provision a wire on a runtime. Contains metadata for attaching the wire to a source transport or component and target transport or
 * component.
 */
public class PhysicalWire {
    private PhysicalWireSource source;
    private PhysicalWireTarget target;

    private final Set<PhysicalOperation> operations;
    private boolean optimizable;

    public PhysicalWire(PhysicalWireSource source, PhysicalWireTarget target, Set<PhysicalOperation> operations) {
        this.source = source;
        this.target = target;
        this.operations = operations;
    }

    /**
     * Returns true if the wire can be optimized.
     *
     * @return true if the wire can be optimized
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the wire can be optimized.
     *
     * @param optimizable whether the wire can be optimized
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    /**
     * Adds an operation definition.
     *
     * @param operation Operation to be added.
     */
    public void addOperation(PhysicalOperation operation) {
        operations.add(operation);
    }


    /**
     * Returns the available operations.
     *
     * @return Collection of operations.
     */
    public Set<PhysicalOperation> getOperations() {
        return operations;
    }

    /**
     * Returns the physical definition for the source side of the wire.
     *
     * @return the physical definition for the source side of the wire
     */
    public PhysicalWireSource getSource() {
        return source;
    }

    /**
     * Returns the physical definition for the target side of the wire.
     *
     * @return the physical definition for the target side of the wire
     */
    public PhysicalWireTarget getTarget() {
        return target;
    }

}
