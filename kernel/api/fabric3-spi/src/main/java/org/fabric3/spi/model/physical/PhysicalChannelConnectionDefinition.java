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
 * Metadata for provisioning a channel connection on a runtime. Specifically, contains metadata for establishing a connection from a source (component producer,
 * channel, or channel binding) to a target (component consumer, channel, or channel binding).
 */
public class PhysicalChannelConnectionDefinition implements Serializable {
    private static final long serialVersionUID = -3810294574460985743L;

    private URI channelUri;
    private boolean bound;
    private PhysicalConnectionSourceDefinition source;
    private PhysicalConnectionTargetDefinition target;
    private PhysicalEventStreamDefinition eventStream;
    private String topic;

    public PhysicalChannelConnectionDefinition(URI channelUri,
                                               PhysicalConnectionSourceDefinition source,
                                               PhysicalConnectionTargetDefinition target,
                                               PhysicalEventStreamDefinition eventStream, boolean bound) {
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

    public PhysicalConnectionSourceDefinition getSource() {
        return source;
    }

    public PhysicalConnectionTargetDefinition getTarget() {
        return target;
    }

    public PhysicalEventStreamDefinition getEventStream() {
        return eventStream;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

}