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

import java.io.Serializable;
import java.net.URI;

/**
 * Metadata for attaching a handler to an event stream.
 */
public class PhysicalHandlerDefinition implements Serializable {
    private static final long serialVersionUID = 660389044446376070L;
    private URI policyClassLoaderId;

    /**
     * Returns the classloader id for the contribution containing the handler. This may be the same as the wire classloader id if the policy is
     * contained in the same user contribution as the source component of the wire.
     *
     * @return the classloader id for the policy
     */
    public URI getPolicyClassLoaderId() {
        return policyClassLoaderId;
    }

    /**
     * Sets the classloader id for the contribution containing the handler. This may be the same as the wire classloader id if the policy is contained
     * in the same user contribution as the source component of the wire.
     *
     * @param id classloader id for the policy
     */
    public void setPolicyClassLoaderId(URI id) {
        this.policyClassLoaderId = id;
    }
}
