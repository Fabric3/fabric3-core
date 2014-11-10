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

import java.net.URI;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Represents the target side of a physical wire.
 */
public abstract class PhysicalWireTargetDefinition extends PhysicalAttachPointDefinition {
    private static final long serialVersionUID = -8430498259706831133L;

    private boolean optimizable;
    private boolean callback;
    private URI callbackUri;

    public PhysicalWireTargetDefinition() {
    }

    public PhysicalWireTargetDefinition(DataType... types) {
        super(types);
    }

    /**
     * Returns the URI for the target callback component for invocations passed through this wire.
     *
     * @return the target callback uri or null if the wire is unidirectional
     */
    public URI getCallbackUri() {
        return callbackUri;
    }

    /**
     * Sets the URI for the target callback component for invocations passed through this wire.
     *
     * @param uri the target callback uri
     */
    public void setCallbackUri(URI uri) {
        this.callbackUri = uri;
    }

    /**
     * Returns true if the wire is a callback wire.
     *
     * @return true if the wire is a callback wire
     */
    public boolean isCallback() {
        return callback;
    }

    /**
     * Sets if the wire is a callback wire.
     *
     * @param callback true if the wire is a callback wire
     */
    public void setCallback(boolean callback) {
        this.callback = callback;
    }

    /**
     * Returns whether the target side of the wire is optimizable.
     *
     * @return true if the target side of the wire is optimizable
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the target side of the wire is optimizable.
     *
     * @param optimizable whether the target side of the wire is optimizable
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

}
