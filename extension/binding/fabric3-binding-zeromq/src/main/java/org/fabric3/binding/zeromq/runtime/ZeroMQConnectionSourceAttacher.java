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

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ZeroMQConnectionSourceAttacher implements SourceConnectionAttacher<ZeroMQConnectionSourceDefinition> {
    private ZeroMQPubSubBroker broker;
    private ClassLoaderRegistry registry;

    public ZeroMQConnectionSourceAttacher(@Reference ZeroMQPubSubBroker broker, @Reference ClassLoaderRegistry registry) {
        this.broker = broker;
        this.registry = registry;
    }

    public void attach(ZeroMQConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {

        ClassLoader loader = registry.getClassLoader(source.getClassLoaderId());
        URI subscriberId = source.getUri();
        ZeroMQMetadata metadata = source.getMetadata();
        broker.subscribe(subscriberId, metadata, connection, loader);
    }

    public void detach(ZeroMQConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ContainerException {
        ZeroMQMetadata metadata = source.getMetadata();
        URI subscriberId = source.getUri();
        broker.unsubscribe(subscriberId, metadata);
    }

}
