/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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

import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.channel.FilterHandler;
import org.fabric3.fabric.channel.ChannelConnectionImpl;
import org.fabric3.fabric.channel.EventStreamImpl;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.channel.EventFilter;
import org.fabric3.spi.builder.channel.EventFilterBuilder;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventFilterDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 * Default ChannelConnector implementation.
 *
 * @version $Rev$ $Date$
 */
public class ChannelConnectorImpl implements ChannelConnector {
    private Map<Class<? extends PhysicalConnectionSourceDefinition>, SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>>
            sourceAttachers;
    private Map<Class<? extends PhysicalConnectionTargetDefinition>, TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>>
            targetAttachers;
    private Map<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>>
            filterBuilders;

    @Reference
    public void setSourceAttachers(Map<Class<? extends PhysicalConnectionSourceDefinition>, SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>> sourceAttachers) {
        this.sourceAttachers = sourceAttachers;
    }

    @Reference
    public void setTargetAttachers(Map<Class<? extends PhysicalConnectionTargetDefinition>, TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>> targetAttachers) {
        this.targetAttachers = targetAttachers;
    }

    @Reference
    public void setFilterBuilders(Map<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>> filterBuilders) {
        this.filterBuilders = filterBuilders;
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

    @SuppressWarnings({"unchecked"})
    private ChannelConnection createConnection(PhysicalChannelConnectionDefinition definition) throws BuilderException {
        ChannelConnection connection = new ChannelConnectionImpl();
        for (PhysicalEventStreamDefinition streamDefinition : definition.getEventStreams()) {
            EventStream stream = new EventStreamImpl(streamDefinition);
            for (PhysicalEventFilterDefinition filterDefinition : streamDefinition.getFilters()) {
                EventFilterBuilder filterBuilder = filterBuilders.get(filterDefinition.getClass());
                EventFilter filter = filterBuilder.build(filterDefinition);
                FilterHandler handler = new FilterHandler(filter);
                stream.addHandler(handler);
            }
            connection.addEventStream(stream);
        }
        return connection;
    }


}
