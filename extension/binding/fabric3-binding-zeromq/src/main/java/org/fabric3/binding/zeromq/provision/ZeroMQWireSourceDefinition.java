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
package org.fabric3.binding.zeromq.provision;

import java.net.URI;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Generated metadata used for attaching a service to a ZeroMQ Socket.
 */
public class ZeroMQWireSourceDefinition extends PhysicalWireSourceDefinition {
    private static final long serialVersionUID = -1119229094076577838L;
    private URI callbackUri;
    private ZeroMQMetadata metadata;

    /**
     * Constructor for a bidirectional service.
     *
     * @param metadata the ZeroMQ metadata to configure the underlying socket
     */
    public ZeroMQWireSourceDefinition(ZeroMQMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Constructor for a bidirectional service.
     *
     * @param callbackUri the callback URI
     * @param metadata    the ZeroMQ metadata to configure the underlying socket
     */
    public ZeroMQWireSourceDefinition(URI callbackUri, ZeroMQMetadata metadata) {
        this.callbackUri = callbackUri;
        this.metadata = metadata;
    }

    public URI getCallbackUri() {
        return callbackUri;
    }

    public ZeroMQMetadata getMetadata() {
        return metadata;
    }

}
