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
package org.fabric3.monitor.impl.builder;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.monitor.impl.model.physical.PhysicalDefaultMonitorDestinationDefinition;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;
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
        EasyMock.expect(appenderBuilder.build(EasyMock.isA(PhysicalAppenderDefinition.class))).andReturn(appender);

        EasyMock.replay(registry, appenderBuilder, eventWriter, appender);
        DefaultMonitorDestinationBuilder builder = new DefaultMonitorDestinationBuilder(registry, eventWriter);

        Map map = Collections.singletonMap(MockDefinition.class, appenderBuilder);
        builder.setAppenderBuilders(map);

        PhysicalDefaultMonitorDestinationDefinition physicalDefinition = new PhysicalDefaultMonitorDestinationDefinition("test");
        physicalDefinition.add(new MockDefinition());
        builder.build(physicalDefinition);

        EasyMock.verify(registry, appenderBuilder, eventWriter, appender);

    }

    private class MockDefinition extends PhysicalAppenderDefinition {
        private static final long serialVersionUID = 8899804812636677852L;
    }
}
