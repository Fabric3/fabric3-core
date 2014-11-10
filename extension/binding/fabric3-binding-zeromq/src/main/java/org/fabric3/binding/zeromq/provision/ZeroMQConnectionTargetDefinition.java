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
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Generated metadata used for attaching producers to a ZeroMQ Socket.
 */
public class ZeroMQConnectionTargetDefinition extends PhysicalConnectionTargetDefinition {
    private static final long serialVersionUID = -3528383965698203784L;
    private ZeroMQMetadata metadata;
    private boolean dedicatedThread;

    /**
     * Constructor for a channel connection.
     *
     * @param uri             the channel URI
     * @param metadata        the ZeroMQ metadata to configure the underlying socket
     * @param dedicatedThread true if the ZeroMQ publisher will always be called on the same thread (e.g. from a ring buffer consumer)
     */
    public ZeroMQConnectionTargetDefinition(URI uri, ZeroMQMetadata metadata, boolean dedicatedThread) {
        this.metadata = metadata;
        this.dedicatedThread = dedicatedThread;
        setUri(uri);
    }

    public ZeroMQMetadata getMetadata() {
        return metadata;
    }

    public boolean isDedicatedThread() {
        return dedicatedThread;
    }
}
