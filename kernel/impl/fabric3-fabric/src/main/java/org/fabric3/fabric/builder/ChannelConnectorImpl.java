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
package org.fabric3.fabric.builder;

import java.util.List;
import java.util.Map;

import org.fabric3.spi.builder.ChannelConnector;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.channel.ChannelConnectionImpl;
import org.fabric3.fabric.channel.EventStreamImpl;
import org.fabric3.fabric.channel.FilterHandler;
import org.fabric3.fabric.channel.TransformerHandler;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.channel.EventFilter;
import org.fabric3.spi.builder.channel.EventFilterBuilder;
import org.fabric3.spi.builder.channel.EventStreamHandlerBuilder;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventFilterDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.physical.PhysicalHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * Default ChannelConnector implementation.
 */
public class ChannelConnectorImpl implements ChannelConnector {
    private Map<Class<? extends PhysicalConnectionSourceDefinition>, SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>> sourceAttachers;
    private Map<Class<? extends PhysicalConnectionTargetDefinition>, TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>> targetAttachers;
    private Map<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>> filterBuilders;
    private Map<Class<? extends PhysicalHandlerDefinition>, EventStreamHandlerBuilder<? extends PhysicalHandlerDefinition>> handlerBuilders;

    private ClassLoaderRegistry classLoaderRegistry;
    private TransformerRegistry transformerRegistry;

    public ChannelConnectorImpl() {
    }

    @Reference
    public void setClassLoaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference
    public void setTransformerRegistry(TransformerRegistry transformerRegistry) {
        this.transformerRegistry = transformerRegistry;
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

    @SuppressWarnings({"unchecked"})
    public void connect(PhysicalChannelConnectionDefinition definition) throws BuilderException {
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
    public void disconnect(PhysicalChannelConnectionDefinition definition) throws BuilderException {
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
     * @throws BuilderException if there is an error creating the connection
     */
    private ChannelConnection createConnection(PhysicalChannelConnectionDefinition definition) throws BuilderException {
        ClassLoader loader = classLoaderRegistry.getClassLoader(definition.getTarget().getClassLoaderId());

        PhysicalEventStreamDefinition streamDefinition = definition.getEventStream();
        EventStream stream = new EventStreamImpl(streamDefinition);
        addTransformer(streamDefinition, stream, loader);
        addFilters(streamDefinition, stream);
        addHandlers(streamDefinition, stream);
        int sequence = definition.getSource().getSequence();

        return new ChannelConnectionImpl(stream, sequence);
    }

    /**
     * Adds event transformers to convert an event from one format to another.
     *
     * @param streamDefinition the stream definition
     * @param stream           the stream being created
     * @param loader           the target classloader to use for the transformation
     * @throws BuilderException if there is an error adding a filter
     */
    @SuppressWarnings({"unchecked"})
    private void addTransformer(PhysicalEventStreamDefinition streamDefinition, EventStream stream, ClassLoader loader) throws BuilderException {
        if (transformerRegistry == null) {
            // no transformer registry configured (e.g. during bootstrap) so skip
            return;
        }
        List<String> eventTypes = streamDefinition.getEventTypes();
        String stringifiedType = eventTypes.get(0);
        try {
            DataType<Object> type = new JavaClass(loader.loadClass(stringifiedType));
            TransformerHandler handler = new TransformerHandler(type, transformerRegistry);
            stream.addHandler(handler);
        } catch (ClassNotFoundException e) {
            throw new BuilderException(e);
        }
    }

    /**
     * Adds event filters if they are defined for the stream.
     *
     * @param streamDefinition the stream definition
     * @param stream           the stream being created
     * @throws BuilderException if there is an error adding a filter
     */
    @SuppressWarnings({"unchecked"})
    private void addFilters(PhysicalEventStreamDefinition streamDefinition, EventStream stream) throws BuilderException {
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
     * @throws BuilderException if there is an error adding a handler
     */
    @SuppressWarnings({"unchecked"})
    private void addHandlers(PhysicalEventStreamDefinition streamDefinition, EventStream stream) throws BuilderException {
        for (PhysicalHandlerDefinition definition : streamDefinition.getHandlers()) {
            EventStreamHandlerBuilder builder = handlerBuilders.get(definition.getClass());
            EventStreamHandler handler = builder.build(definition);
            stream.addHandler(handler);
        }
    }

}
