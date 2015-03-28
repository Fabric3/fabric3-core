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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.container.channel.ChannelConnectionImpl;
import org.fabric3.fabric.container.channel.EventStreamImpl;
import org.fabric3.fabric.container.channel.FilterHandler;
import org.fabric3.spi.container.builder.ChannelConnector;
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.builder.channel.EventFilterBuilder;
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
import org.fabric3.spi.model.physical.PhysicalEventFilter;
import org.fabric3.spi.model.physical.PhysicalEventStream;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default ChannelConnector implementation.
 */
public class ChannelConnectorImpl implements ChannelConnector {

    @Reference
    protected ChannelManager channelManager;

    @Reference(required = false)
    protected Map<Class<?>, SourceConnectionAttacher<?>> sourceAttachers = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, TargetConnectionAttacher<?>> targetAttachers = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, EventFilterBuilder<?>> filterBuilders = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, DirectConnectionFactory> connectionFactories = new HashMap<>();

    @Reference
    protected TransformerHandlerFactory transformerHandlerFactory;

    @SuppressWarnings({"unchecked"})
    public void connect(PhysicalChannelConnection physicalConnection) {
        PhysicalConnectionSource source = physicalConnection.getSource();
        PhysicalConnectionTarget target = physicalConnection.getTarget();
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
    }

    @SuppressWarnings({"unchecked"})
    public void disconnect(PhysicalChannelConnection physicalConnection) {
        PhysicalConnectionSource source = physicalConnection.getSource();
        PhysicalConnectionTarget target = physicalConnection.getTarget();
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
                Class<?> type;
                if (source.isDirectConnection()) {
                    type = source.getServiceInterface();
                } else {
                    type = target.getServiceInterface();
                }
                DirectConnectionFactory factory = connectionFactories.get(type);
                if (factory == null) {
                    throw new Fabric3Exception("Factory type not found: " + type.getName());
                }
                Class<?> interfaze = source.isDirectConnection() ? source.getServiceInterface() : target.getServiceInterface();
                supplier = factory.getConnection(channelUri, interfaze);
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
            PhysicalEventStream physicalStream = physicalConnection.getEventStream();
            EventStream stream = new EventStreamImpl(physicalStream);
            addTypeTransformer(physicalConnection, stream, loader);
            addFilters(physicalStream, stream);
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
        List<Class<?>> eventTypes = stream.getDefinition().getEventTypes().stream().collect(Collectors.toList());
        EventStreamHandler handler = transformerHandlerFactory.createHandler(sourceType, targetType, eventTypes, loader);
        stream.addHandler(handler);
    }

    /**
     * Adds event filters if they are defined for the stream.
     *
     * @param physicalStream the physical stream
     * @param stream         the stream being created
     * @ if there is an error adding a filter
     */
    @SuppressWarnings({"unchecked"})
    private void addFilters(PhysicalEventStream physicalStream, EventStream stream) {
        for (PhysicalEventFilter physicalFilter : physicalStream.getFilters()) {
            EventFilterBuilder builder = filterBuilders.get(physicalFilter.getClass());
            EventFilter filter = builder.build(physicalFilter);
            FilterHandler handler = new FilterHandler(filter);
            stream.addHandler(handler);
        }
    }

}
