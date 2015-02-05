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
package org.fabric3.binding.jms.runtime;

import javax.jms.ConnectionFactory;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.DeliveryMode;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.HeadersDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.jms.runtime.channel.JmsEventStreamHandler;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.provision.JmsConnectionTarget;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches a producer to a JMS destination.
 */
@Key("org.fabric3.binding.jms.spi.provision.JmsConnectionTarget")
public class JmsConnectionTargetAttacher implements TargetConnectionAttacher<JmsConnectionTarget> {
    private AdministeredObjectResolver resolver;

    public JmsConnectionTargetAttacher(@Reference AdministeredObjectResolver resolver, @Reference ChannelManager channelManager) {
        this.resolver = resolver;
    }

    public void attach(PhysicalConnectionSourceDefinition source, JmsConnectionTarget target, ChannelConnection connection) throws Fabric3Exception {

        // resolve the connection factories and destinations
        JmsBindingMetadata metadata = target.getMetadata();
        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        HeadersDefinition headers = metadata.getHeaders();
        boolean persistent = DeliveryMode.PERSISTENT == headers.getDeliveryMode() || headers.getDeliveryMode() == null;
        javax.jms.Destination destination;
        ConnectionFactory connectionFactory;
        connectionFactory = resolver.resolve(connectionFactoryDefinition);
        Destination destinationDefinition = metadata.getDestination();
        destination = resolver.resolve(destinationDefinition, connectionFactory);
        EventStream stream = connection.getEventStream();
        JmsEventStreamHandler handler = new JmsEventStreamHandler(destination, connectionFactory, persistent);
        stream.addHandler(handler);
    }

    public void detach(PhysicalConnectionSourceDefinition source, JmsConnectionTarget target) throws Fabric3Exception {
        resolver.release(target.getMetadata().getConnectionFactory());
    }

}