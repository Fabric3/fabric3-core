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
 */
package org.fabric3.fabric.container.builder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.container.channel.ChannelConnectionImpl;
import org.fabric3.fabric.container.channel.EventStreamImpl;
import org.fabric3.spi.container.builder.component.DirectConnectionFactory;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default ChannelConnector implementation.
 *
 * This implementation caches connections for reuse. For example, if two bound consumers are connected to the same channel, two connections will be generated
 * for each consumer: one from the binding to the channel and one from the channel to the target component. In this case, only one connection should be engaged
 * from the binding to the channel (otherwise duplicate messages will be received by the components). For the second component, the cached channel will be
 * returned instead of creating an additional one.
 *
 * Channels are cached based on their source id/target id pair. Bindings will generally only engage one connection from the transport to the channel.
 * Connections from channels to components will generally always be engaged (i.e. their target ids will be unique) since a component will need to be injected
 * with the connection proxy.
 */
public class ChannelConnectorImpl implements ChannelConnector {
    private Map<Class<?>, DirectConnectionFactory> connectionFactories = new HashMap<>();

    private Map<Key, Holder> cachedConnections = new HashMap<>();  // connection cache

    @Reference
    protected ChannelManager channelManager;

    @Reference(required = false)
    protected Map<Class<?>, SourceConnectionAttacher<?>> sourceAttachers = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, TargetConnectionAttacher<?>> targetAttachers = new HashMap<>();

    @Reference
    protected TransformerHandlerFactory transformerHandlerFactory;

    @Reference(required = false)
    public void setConnectionFactories(List<DirectConnectionFactory> factories) {
        this.connectionFactories.clear();
        factories.forEach(factory -> factory.getTypes().forEach(type -> connectionFactories.put(type, factory)));
    }

    @SuppressWarnings({"unchecked"})
    public ChannelConnection connect(PhysicalChannelConnection physicalConnection) {
        PhysicalConnectionSource source = physicalConnection.getSource();
        PhysicalConnectionTarget target = physicalConnection.getTarget();
        Key key = new Key(source.getSourceId(), target.getTargetId());
        Holder holder = cachedConnections.get(key);
        if (holder != null) {
            // connection is cached; don't engage a second one
            holder.count.incrementAndGet();
            return holder.connection;
        }
        SourceConnectionAttacher sourceAttacher = sourceAttachers.get(source.getClass());
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Attacher not found for type: " + source.getClass().getName());
        }
        TargetConnectionAttacher targetAttacher = targetAttachers.get(target.getClass());
        if (targetAttacher == null) {
            throw new Fabric3Exception("Attacher not found for type: " + target.getClass().getName());
        }

        ChannelConnection connection = createConnection(physicalConnection);

        sourceAttacher.attach(source, target, connection);
        targetAttacher.attach(source, target, connection);
        cachedConnections.put(key, new Holder(connection));
        return connection;
    }

    @SuppressWarnings({"unchecked"})
    public void disconnect(PhysicalChannelConnection physicalConnection) {
        PhysicalConnectionSource source = physicalConnection.getSource();
        PhysicalConnectionTarget target = physicalConnection.getTarget();
        Key key = new Key(source.getSourceId(), target.getTargetId());
        Holder holder = cachedConnections.get(key);
        if (holder == null) {
            return;
        }
        if (holder.count.decrementAndGet() == 0) {
            cachedConnections.remove(key);
        } else {
            return;
        }
        SourceConnectionAttacher sourceAttacher = sourceAttachers.get(source.getClass());
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Attacher not found for type: " + source.getClass().getName());
        }
        TargetConnectionAttacher targetAttacher = targetAttachers.get(target.getClass());
        if (targetAttacher == null) {
            throw new Fabric3Exception("Attacher not found for type: " + target.getClass().getName());
        }
        sourceAttacher.detach(source, target);
        targetAttacher.detach(source, target);
    }

    /**
     * Creates the connection.
     *
     * @param physicalConnection the connection
     * @return the connection
     * @ if there is an error creating the connection
     */
    private ChannelConnection createConnection(PhysicalChannelConnection physicalConnection) {
        PhysicalConnectionSource source = physicalConnection.getSource();
        PhysicalConnectionTarget target = physicalConnection.getTarget();
        if (source.isDirectConnection() || target.isDirectConnection()) {
            // handle direct connection
            int sequence = source.getSequence();
            URI channelUri = physicalConnection.getChannelUri();

            Supplier<?> supplier;
            if (physicalConnection.isBound()) {
                // get the direct connection from the binding
                Class<?> type = source.isDirectConnection() ? source.getServiceInterface() : target.getServiceInterface();
                DirectConnectionFactory factory = connectionFactories.get(type);
                if (factory == null) {
                    throw new Fabric3Exception("Factory type not found: " + type.getName());
                }
                URI attachUri = physicalConnection.getAttachUri();
                // Return a Supplier of a Supplier to lazily initialize the connection. This is so the source attachment can be done before this call to
                // getConnection. Otherwise, DirectConnectionFactory.getConnection() will be called before the source is attached and channel resources
                // can be created
                supplier = () -> factory.getConnection(channelUri, attachUri, type).get();
            } else {
                // get the direct connection to the local channel
                Channel channel = channelManager.getChannel(channelUri, ChannelSide.COLLOCATED);
                if (channel == null) {
                    throw new Fabric3Exception("Channel not found: " + channelUri);
                }
                supplier = channel::getDirectConnection;
            }

            return new ChannelConnectionImpl(supplier, sequence);
        } else {
            // connect using an event stream
            ClassLoader loader = physicalConnection.getTarget().getClassLoader();
            Class<?> eventType = physicalConnection.getEventType();
            EventStream stream = new EventStreamImpl(eventType);
            addTypeTransformer(physicalConnection, stream, loader);
            int sequence = source.getSequence();
            return new ChannelConnectionImpl(stream, sequence);
        }
    }

    private void addTypeTransformer(PhysicalChannelConnection connection, EventStream stream, ClassLoader loader) {
        if (transformerHandlerFactory == null) {
            return;  // bootstrap
        }
        List<DataType> sourceTypes = connection.getSource().getDataTypes();
        List<DataType> targetTypes = connection.getTarget().getDataTypes();
        if (sourceTypes.isEmpty() || targetTypes.isEmpty()) {
            return;
        }
        if (sourceTypes.size() > 1 || targetTypes.size() > 1) {
            // for now, only support one data type
            throw new Fabric3Exception("Multi-type events are not supported");
        }
        DataType sourceType = sourceTypes.get(0);
        DataType targetType = targetTypes.get(0);
        if (sourceType.equals(targetType)) {
            return;
        }
        List<Class<?>> eventTypes = Collections.singletonList(stream.getEventType());
        EventStreamHandler handler = transformerHandlerFactory.createHandler(sourceType, targetType, eventTypes, loader);
        stream.addHandler(handler);
    }

    private class Key {
        String sourceId;
        String targetId;

        public Key(String sourceId, String targetId) {
            this.sourceId = sourceId;
            this.targetId = targetId;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key key = (Key) o;

            return sourceId.equals(key.sourceId) && targetId.equals(key.targetId);
        }

        public int hashCode() {
            int result = sourceId.hashCode();
            result = 31 * result + targetId.hashCode();
            return result;
        }
    }

    private class Holder {
        AtomicInteger count = new AtomicInteger(1);
        ChannelConnection connection;

        public Holder(ChannelConnection connection) {
            this.connection = connection;
        }
    }

}
