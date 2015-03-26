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
package org.fabric3.monitor.impl.builder;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.monitor.impl.model.physical.PhysicalDefaultMonitorDestination;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.model.physical.PhysicalAppender;
import org.fabric3.monitor.spi.writer.EventWriter;

/**
 *
 */
public class DefaultMonitorDestinationBuilderTestCase extends TestCase {

    @SuppressWarnings("unchecked")
    public void testBuild() throws Exception {

        MonitorDestinationRegistry registry = EasyMock.createMock(MonitorDestinationRegistry.class);
        registry.register(EasyMock.isA(MonitorDestination.class));

        EventWriter eventWriter = EasyMock.createMock(EventWriter.class);

        Appender appender = EasyMock.createMock(Appender.class);
        appender.start();
        AppenderBuilder appenderBuilder = EasyMock.createMock(AppenderBuilder.class);
        EasyMock.expect(appenderBuilder.build(EasyMock.isA(PhysicalAppender.class))).andReturn(appender);

        EasyMock.replay(registry, appenderBuilder, eventWriter, appender);
        DefaultMonitorDestinationBuilder builder = new DefaultMonitorDestinationBuilder(registry, eventWriter);

        Map map = Collections.singletonMap(Mock.class, appenderBuilder);
        builder.setAppenderBuilders(map);

        PhysicalDefaultMonitorDestination physicalDestination = new PhysicalDefaultMonitorDestination("test");
        physicalDestination.add(new Mock());
        builder.build(physicalDestination);

        EasyMock.verify(registry, appenderBuilder, eventWriter, appender);

    }

    private class Mock extends PhysicalAppender {
    }
}
