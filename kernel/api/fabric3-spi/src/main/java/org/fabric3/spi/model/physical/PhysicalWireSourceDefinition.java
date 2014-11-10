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

import org.fabric3.api.model.type.contract.DataType;

/**
 * Represents the source side of a wire.
 */
public abstract class PhysicalWireSourceDefinition extends PhysicalAttachPointDefinition {
    private static final long serialVersionUID = 2560576437284123839L;

    private boolean optimizable;
    private String key;
    private int order = Integer.MIN_VALUE;

    public PhysicalWireSourceDefinition() {
    }

    public PhysicalWireSourceDefinition(DataType... types) {
        super(types);
    }

    /**
     * Returns whether the source side of the wire is optimizable.
     *
     * @return true if the source side of the wire is optimizable
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the source side of the wire is optimizable.
     *
     * @param optimizable whether the source side of the wire is optimizable
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    /**
     * Returns the key to be used when this wire is part of a Map-style reference.
     *
     * @return the key to be used when this wire is part of a Map-style reference
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key to be used when this wire is part of a Map-style reference.
     *
     * @param key the key to be used when this wire is part of a Map-style reference
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the order for collection- and array-based wires or {@link Integer#MIN_VALUE} if not specified.
     *
     * @return the order for collection- and array-based wires or {@link Integer#MIN_VALUE} if not specified
     */
    public int getOrder() {
        return order;
    }

    /**
     * Returns true if the wire is ordered.
     *
     * @return true if the wire is ordered
     */
    public boolean isOrdered() {
        return order != Integer.MIN_VALUE;
    }

    /**
     * Sets the order for collection- and array-based wires.
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
