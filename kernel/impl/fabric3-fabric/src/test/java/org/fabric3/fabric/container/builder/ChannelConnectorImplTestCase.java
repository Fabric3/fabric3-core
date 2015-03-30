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
package org.fabric3.fabric.container.builder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.builder.channel.EventFilterBuilder;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.model.physical.PhysicalChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalEventFilter;
import org.fabric3.spi.model.physical.PhysicalEventStream;

/**
 *
 */
public class ChannelConnectorImplTestCase extends TestCase {
    private ChannelConnectorImpl connector;
    private SourceConnectionAttacher sourceAttacher;
    private TargetConnectionAttacher targetAttacher;
    private PhysicalChannelConnection connection;

    @SuppressWarnings({"unchecked"})
    public void testConnect() throws Exception {
        sourceAttacher.attach(EasyMock.isA(PhysicalConnectionSource.class),
                              EasyMock.isA(PhysicalConnectionTarget.class),
                              EasyMock.isA(ChannelConnection.class));
        targetAttacher.attach(EasyMock.isA(PhysicalConnectionSource.class),
                              EasyMock.isA(PhysicalConnectionTarget.class),
                              EasyMock.isA(ChannelConnection.class));

        EventFilterBuilder filterBuilder = EasyMock.createMock(EventFilterBuilder.class);
        EasyMock.expect(filterBuilder.build(EasyMock.isA(MockFilter.class))).andReturn(new MockEventFilter());

        connector.filterBuilders = Collections.singletonMap(MockFilter.class, filterBuilder);

        EasyMock.replay(sourceAttacher, targetAttacher, filterBuilder);

        connector.connect(connection);

        EasyMock.verify(sourceAttacher, targetAttacher, filterBuilder);
    }

    @SuppressWarnings({"unchecked"})
    public void testDisconnect() throws Exception {
        sourceAttacher.detach(EasyMock.isA(PhysicalConnectionSource.class), EasyMock.isA(PhysicalConnectionTarget.class));
        targetAttacher.detach(EasyMock.isA(PhysicalConnectionSource.class), EasyMock.isA(PhysicalConnectionTarget.class));

        EasyMock.replay(sourceAttacher, targetAttacher);

        connector.disconnect(connection);

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
        Map sourceAttachers = Collections.singletonMap(MockSource.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockTarget.class, targetAttacher);
        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;
        connection = createConnection();
    }

    private PhysicalChannelConnection createConnection() {
        PhysicalConnectionSource source = new MockSource();
        source.setClassLoader(getClass().getClassLoader());
        PhysicalConnectionTarget target = new MockTarget();
        target.setClassLoader(getClass().getClassLoader());
        PhysicalEventStream stream = new PhysicalEventStream("stream");
        stream.addEventType(Object.class);
        stream.addFilter(new MockFilter());
        URI uri = URI.create("testChannel");
        return new PhysicalChannelConnection(uri, URI.create("test"), source, target, stream, false);
    }

    private class MockSource extends PhysicalConnectionSource {

    }

    private class MockTarget extends PhysicalConnectionTarget {

    }

    private class MockFilter extends PhysicalEventFilter {
    }

    private class MockEventFilter implements EventFilter {

        public boolean filter(Object event) {
            return false;
        }
    }

}
