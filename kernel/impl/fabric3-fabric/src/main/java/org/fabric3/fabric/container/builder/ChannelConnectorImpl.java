/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.container.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.container.channel.ChannelConnectionImpl;
import org.fabric3.fabric.container.channel.EventStreamImpl;
import org.fabric3.fabric.container.channel.FilterHandler;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.ChannelConnector;
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.builder.channel.EventFilterBuilder;
import org.fabric3.spi.container.builder.channel.EventStreamHandlerBuilder;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.HandlerCreationException;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventFilterDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.physical.PhysicalHandlerDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default ChannelConnector implementation.
 */
public class ChannelConnectorImpl implements ChannelConnector {
    private Map<Class<? extends PhysicalConnectionSourceDefinition>, SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>> sourceAttachers;
    private Map<Class<? extends PhysicalConnectionTargetDefinition>, TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>> targetAttachers;
    private Map<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>> filterBuilders;
    private Map<Class<? extends PhysicalHandlerDefinition>, EventStreamHandlerBuilder<? extends PhysicalHandlerDefinition>> handlerBuilders;

    private ClassLoaderRegistry classLoaderRegistry;
    private TransformerHandlerFactory transformerHandlerFactory;

    public ChannelConnectorImpl() {
    }

    @Reference
    public void setClassLoaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference(required = false)
    public void setSourceAttachers(Map<Class<? extends PhysicalConnectionSourceDefinition>,
            SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>> sourceAttachers) {
        this.sourceAttachers = sourceAttachers;
    }

    @Reference(required = false)
    public void setTargetAttachers(Map<Class<? extends PhysicalConnectionTargetDefinition>,
            TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>> targetAttachers) {
        this.targetAttachers = targetAttachers;
    }

    @Reference(required = false)
    public void setFilterBuilders(Map<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>>
                                              filterBuilders) {
        this.filterBuilders = filterBuilders;
    }

    @Reference(required = false)
    public void setHandlerBuilders(Map<Class<? extends PhysicalHandlerDefinition>, EventStreamHandlerBuilder<? extends PhysicalHandlerDefinition>>
                                               handlerBuilders) {
        this.handlerBuilders = handlerBuilders;
    }

    @Reference(required = false)
    public void setTransformerHandlerFactory(TransformerHandlerFactory factory) {
        this.transformerHandlerFactory = factory;
    }

    @SuppressWarnings({"unchecked"})
    public void connect(PhysicalChannelConnectionDefinition definition) throws ContainerException {
        PhysicalConnectionSourceDefinition source = definition.getSource();
        PhysicalConnectionTargetDefinition target = definition.getTarget();
        SourceConnectionAttacher sourceAttacher = sourceAttachers.get(source.getClass());
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Attacher not found for type: " + source.getClass().getName());
        }
        TargetConnectionAttacher targetAttacher = targetAttachers.get(target.getClass());
        if (targetAttacher == null) {
            throw new AttacherNotFoundException("Attacher not found for type: " + target.getClass().getName());
        }

        ChannelConnection connection = createConnection(definition);

        sourceAttacher.attach(source, target, connection);
        targetAttacher.attach(source, target, connection);
    }

    @SuppressWarnings({"unchecked"})
    public void disconnect(PhysicalChannelConnectionDefinition definition) throws ContainerException {
        PhysicalConnectionSourceDefinition source = definition.getSource();
        PhysicalConnectionTargetDefinition target = definition.getTarget();
        SourceConnectionAttacher sourceAttacher = sourceAttachers.get(source.getClass());
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Attacher not found for type: " + source.getClass().getName());
        }
        TargetConnectionAttacher targetAttacher = targetAttachers.get(target.getClass());
        if (targetAttacher == null) {
            throw new AttacherNotFoundException("Attacher not found for type: " + target.getClass().getName());
        }
        sourceAttacher.detach(source, target);
        targetAttacher.detach(source, target);
    }

    /**
     * Creates the connection.
     *
     * @param definition the connection definition
     * @return the connection
     * @throws ContainerException if there is an error creating the connection
     */
    private ChannelConnection createConnection(PhysicalChannelConnectionDefinition definition) throws ContainerException {
        ClassLoader loader = classLoaderRegistry.getClassLoader(definition.getTarget().getClassLoaderId());

        PhysicalEventStreamDefinition streamDefinition = definition.getEventStream();
        EventStream stream = new EventStreamImpl(streamDefinition);
        addTypeTransformer(definition, stream, loader);
        addFilters(streamDefinition, stream);
        addHandlers(streamDefinition, stream);
        int sequence = definition.getSource().getSequence();

        return new ChannelConnectionImpl(stream, sequence);
    }

    private void addTypeTransformer(PhysicalChannelConnectionDefinition definition, EventStream stream, ClassLoader loader) throws ContainerException {
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
            throw new ContainerException("Multi-type events are not supported");
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
        } catch (HandlerCreationException | ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    /**
     * Adds event filters if they are defined for the stream.
     *
     * @param streamDefinition the stream definition
     * @param stream           the stream being created
     * @throws ContainerException if there is an error adding a filter
     */
    @SuppressWarnings({"unchecked"})
    private void addFilters(PhysicalEventStreamDefinition streamDefinition, EventStream stream) throws ContainerException {
        for (PhysicalEventFilterDefinition definition : streamDefinition.getFilters()) {
            EventFilterBuilder builder = filterBuilders.get(definition.getClass());
            EventFilter filter = builder.build(definition);
            FilterHandler handler = new FilterHandler(filter);
            stream.addHandler(handler);
        }
    }

    /**
     * Adds event stream handlers if they are defined for the stream.
     *
     * @param streamDefinition the stream definition
     * @param stream           the stream being created
     * @throws ContainerException if there is an error adding a handler
     */
    @SuppressWarnings({"unchecked"})
    private void addHandlers(PhysicalEventStreamDefinition streamDefinition, EventStream stream) throws ContainerException {
        for (PhysicalHandlerDefinition definition : streamDefinition.getHandlers()) {
            EventStreamHandlerBuilder builder = handlerBuilders.get(definition.getClass());
            EventStreamHandler handler = builder.build(definition);
            stream.addHandler(handler);
        }
    }

}