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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.container.channel.ChannelConnectionImpl;
import org.fabric3.fabric.container.channel.EventStreamImpl;
import org.fabric3.fabric.container.channel.FilterHandler;
import org.fabric3.spi.container.builder.ChannelConnector;
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.builder.channel.EventFilterBuilder;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventFilterDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default ChannelConnector implementation.
 */
public class ChannelConnectorImpl implements ChannelConnector {
    @Reference(required = false)
    protected Map<Class<?>, SourceConnectionAttacher<?>> sourceAttachers = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, TargetConnectionAttacher<?>> targetAttachers = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, EventFilterBuilder<?>> filterBuilders = new HashMap<>();

    @Reference
    protected TransformerHandlerFactory transformerHandlerFactory;

    @SuppressWarnings({"unchecked"})
    public void connect(PhysicalChannelConnectionDefinition definition) {
        PhysicalConnectionSourceDefinition source = definition.getSource();
        PhysicalConnectionTargetDefinition target = definition.getTarget();
        SourceConnectionAttacher sourceAttacher = sourceAttachers.get(source.getClass());
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Attacher not found for type: " + source.getClass().getName());
        }
        TargetConnectionAttacher targetAttacher = targetAttachers.get(target.getClass());
        if (targetAttacher == null) {
            throw new Fabric3Exception("Attacher not found for type: " + target.getClass().getName());
        }

        ChannelConnection connection = createConnection(definition);

        sourceAttacher.attach(source, target, connection);
        targetAttacher.attach(source, target, connection);
    }

    @SuppressWarnings({"unchecked"})
    public void disconnect(PhysicalChannelConnectionDefinition definition) {
        PhysicalConnectionSourceDefinition source = definition.getSource();
        PhysicalConnectionTargetDefinition target = definition.getTarget();
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
     * @param definition the connection definition
     * @return the connection
     * @ if there is an error creating the connection
     */
    private ChannelConnection createConnection(PhysicalChannelConnectionDefinition definition) {
        ClassLoader loader = definition.getTarget().getClassLoader();

        PhysicalEventStreamDefinition streamDefinition = definition.getEventStream();
        EventStream stream = new EventStreamImpl(streamDefinition);
        addTypeTransformer(definition, stream, loader);
        addFilters(streamDefinition, stream);
        int sequence = definition.getSource().getSequence();

        return new ChannelConnectionImpl(stream, sequence);
    }

    private void addTypeTransformer(PhysicalChannelConnectionDefinition definition, EventStream stream, ClassLoader loader) {
        if (transformerHandlerFactory == null) {
            return;  // bootstrap
        }
        List<DataType> sourceTypes = definition.getSource().getDataTypes();
        List<DataType> targetTypes = definition.getTarget().getDataTypes();
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
        try {
            List<Class<?>> eventTypes = new ArrayList<>();
            for (String type : stream.getDefinition().getEventTypes()) {
                Class<?> clazz = loader.loadClass(type);
                eventTypes.add(clazz);
            }
            EventStreamHandler handler = transformerHandlerFactory.createHandler(sourceType, targetType, eventTypes, loader);
            stream.addHandler(handler);
        } catch (ClassNotFoundException e) {
            throw new Fabric3Exception(e);
        }
    }

    /**
     * Adds event filters if they are defined for the stream.
     *
     * @param streamDefinition the stream definition
     * @param stream           the stream being created
     * @ if there is an error adding a filter
     */
    @SuppressWarnings({"unchecked"})
    private void addFilters(PhysicalEventStreamDefinition streamDefinition, EventStream stream) {
        for (PhysicalEventFilterDefinition definition : streamDefinition.getFilters()) {
            EventFilterBuilder builder = filterBuilders.get(definition.getClass());
            EventFilter filter = builder.build(definition);
            FilterHandler handler = new FilterHandler(filter);
            stream.addHandler(handler);
        }
    }

}
