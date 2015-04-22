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
package org.fabric3.binding.zeromq.runtime;

import java.net.URI;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionSource;
import org.fabric3.spi.container.builder.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@Key("org.fabric3.binding.zeromq.provision.ZeroMQConnectionSource")
public class ZeroMQConnectionSourceAttacher implements SourceConnectionAttacher<ZeroMQConnectionSource> {
    private ZeroMQPubSubBroker broker;

    public ZeroMQConnectionSourceAttacher(@Reference ZeroMQPubSubBroker broker) {
        this.broker = broker;
    }

    public void attach(ZeroMQConnectionSource source, PhysicalConnectionTarget target, ChannelConnection connection) {
        ClassLoader loader = source.getClassLoader();
        URI subscriberId = source.getUri();
        ZeroMQMetadata metadata = source.getMetadata();
        broker.subscribe(subscriberId, metadata, connection, loader);
    }

    public void detach(ZeroMQConnectionSource source, PhysicalConnectionTarget target) {
        ZeroMQMetadata metadata = source.getMetadata();
        URI subscriberId = source.getUri();
        broker.unsubscribe(subscriberId, metadata);
    }

}
