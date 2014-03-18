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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.container.builder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.builder.channel.EventFilterBuilder;
import org.fabric3.spi.container.builder.channel.EventStreamHandlerBuilder;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.PassThroughHandler;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventFilterDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.physical.PhysicalHandlerDefinition;

/**
 *
 */
public class ChannelConnectorImplTestCase extends TestCase {
    private ChannelConnectorImpl connector;
    private SourceConnectionAttacher sourceAttacher;
    private TargetConnectionAttacher targetAttacher;
    private PhysicalChannelConnectionDefinition definition;

    @SuppressWarnings({"unchecked"})
    public void testConnect() throws Exception {
        sourceAttacher.attach(EasyMock.isA(PhysicalConnectionSourceDefinition.class),
                              EasyMock.isA(PhysicalConnectionTargetDefinition.class),
                              EasyMock.isA(ChannelConnection.class));
        targetAttacher.attach(EasyMock.isA(PhysicalConnectionSourceDefinition.class),
                              EasyMock.isA(PhysicalConnectionTargetDefinition.class),
                              EasyMock.isA(ChannelConnection.class));

        EventFilterBuilder filterBuilder = EasyMock.createMock(EventFilterBuilder.class);
        EasyMock.expect(filterBuilder.build(EasyMock.isA(MockFilterDefinition.class))).andReturn(new MockEventFilter());

        EventStreamHandlerBuilder handlerBuilder = EasyMock.createMock(EventStreamHandlerBuilder.class);
        EasyMock.expect(handlerBuilder.build(EasyMock.isA(MockHandlerDefinition.class))).andReturn(new PassThroughHandler());

        Map filterBuilderMap = Collections.singletonMap(MockFilterDefinition.class, filterBuilder);
        connector.setFilterBuilders(filterBuilderMap);
        Map handlerMap = Collections.singletonMap(MockHandlerDefinition.class, handlerBuilder);
        connector.setHandlerBuilders(handlerMap);

        EasyMock.replay(sourceAttacher, targetAttacher, handlerBuilder, filterBuilder);

        connector.connect(definition);

        EasyMock.verify(sourceAttacher, targetAttacher, handlerBuilder, filterBuilder);
    }

    @SuppressWarnings({"unchecked"})
    public void testDisconnect() throws Exception {
        sourceAttacher.detach(EasyMock.isA(PhysicalConnectionSourceDefinition.class), EasyMock.isA(PhysicalConnectionTargetDefinition.class));
        targetAttacher.detach(EasyMock.isA(PhysicalConnectionSourceDefinition.class), EasyMock.isA(PhysicalConnectionTargetDefinition.class));

        EasyMock.replay(sourceAttacher, targetAttacher);

        connector.disconnect(definition);

        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
        ClassLoaderRegistry classLoaderRegistry = EasyMock.createMock((ClassLoaderRegistry.class));
        EasyMock.expect(classLoaderRegistry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader()).anyTimes();

        EasyMock.replay(classLoaderRegistry);

        sourceAttacher = EasyMock.createMock(SourceConnectionAttacher.class);
        targetAttacher = EasyMock.createMock(TargetConnectionAttacher.class);

        connector = new ChannelConnectorImpl();
        connector.setClassLoaderRegistry(classLoaderRegistry);
        Map sourceAttachers = Collections.singletonMap(MockSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockTargetDefinition.class, targetAttacher);
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);
        definition = createDefinition();
    }

    private PhysicalChannelConnectionDefinition createDefinition() {
        URI classLoaderUri = URI.create("classLoaderUri");
        PhysicalConnectionSourceDefinition sourceDefinition = new MockSourceDefinition();
        sourceDefinition.setClassLoaderId(classLoaderUri);
        PhysicalConnectionTargetDefinition targetDefinition = new MockTargetDefinition();
        targetDefinition.setClassLoaderId(classLoaderUri);
        PhysicalEventStreamDefinition stream = new PhysicalEventStreamDefinition("stream");
        stream.addEventType("java.lang.Object");
        stream.addHandlerDefinition(new MockHandlerDefinition());
        stream.addFilterDefinition(new MockFilterDefinition());
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, stream);
    }

    private class MockSourceDefinition extends PhysicalConnectionSourceDefinition {
        private static final long serialVersionUID = 3221998280377320208L;

    }

    private class MockTargetDefinition extends PhysicalConnectionTargetDefinition {
        private static final long serialVersionUID = 3221998280377320208L;

    }

    private class MockFilterDefinition extends PhysicalEventFilterDefinition {
        private static final long serialVersionUID = 4679150177565902805L;
    }

    private class MockHandlerDefinition extends PhysicalHandlerDefinition {
        private static final long serialVersionUID = -460538402100677668L;
    }

    private class MockEventFilter implements EventFilter {

        public boolean filter(Object event) {
            return false;
        }
    }

}
