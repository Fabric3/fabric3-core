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

/**
 * Metadata for provisioning a channel connection on a runtime. Specifically, contains metadata for establishing a connection from a source (component producer,
 * channel, or channel binding) to a target (component consumer, channel, or channel binding).
 */
public class PhysicalChannelConnection {
    private URI channelUri;
    private boolean bound;
    private PhysicalConnectionSource source;
    private PhysicalConnectionTarget target;
    private PhysicalEventStream eventStream;

    public PhysicalChannelConnection(URI channelUri,
                                     PhysicalConnectionSource source,
                                     PhysicalConnectionTarget target,
                                     PhysicalEventStream eventStream,
                                     boolean bound) {
        this.channelUri = channelUri;
        this.source = source;
        this.target = target;
        this.eventStream = eventStream;
        this.bound = bound;
    }

    public URI getChannelUri() {
        return channelUri;
    }

    public boolean isBound() {
        return bound;
    }

    public PhysicalConnectionSource getSource() {
        return source;
    }

    public PhysicalConnectionTarget getTarget() {
        return target;
    }

    public PhysicalEventStream getEventStream() {
        return eventStream;
    }

}