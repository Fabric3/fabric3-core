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
package org.fabric3.monitor.impl.appender.factory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.model.type.ModelObject;
import org.fabric3.monitor.impl.appender.console.ConsoleAppenderDefinition;
import org.fabric3.monitor.impl.appender.console.PhysicalConsoleAppenderDefinition;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.monitor.spi.appender.AppenderGenerator;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class AppenderFactoryImplTestCase extends TestCase {
    private static final String APPENDER = "<appenders><appender.console/></appenders>";

    private AppenderFactoryImpl factory;
    private AppenderFactoryMonitor monitor;
    private LoaderRegistry loaderRegistry;
    private AppenderGenerator<ConsoleAppenderDefinition> generator;
    private AppenderBuilder<PhysicalConsoleAppenderDefinition> builder;

    public void testCreateAppender() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(APPENDER.getBytes()));

        ConsoleAppenderDefinition appenderDefinition = new ConsoleAppenderDefinition();
        EasyMock.expect(loaderRegistry.load(EasyMock.eq(reader), EasyMock.eq(ModelObject.class), EasyMock.isA(IntrospectionContext.class))).andReturn(
                appenderDefinition);

        PhysicalConsoleAppenderDefinition physicalDefinition = new PhysicalConsoleAppenderDefinition();
        EasyMock.expect(generator.generateResource(EasyMock.isA(ConsoleAppenderDefinition.class))).andReturn(physicalDefinition);

        Appender appender = EasyMock.createMock(Appender.class);
        EasyMock.expect(builder.build(physicalDefinition)).andReturn(appender);

        EasyMock.replay(loaderRegistry, generator, builder, monitor, appender);

        List<Appender> appenders = factory.instantiate(reader);
        assertEquals(appender, appenders.get(0));

        EasyMock.verify(loaderRegistry, generator, builder, monitor, appender);
    }

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        monitor = EasyMock.createMock(AppenderFactoryMonitor.class);

        factory = new AppenderFactoryImpl(loaderRegistry, monitor);

        generator = EasyMock.createMock(AppenderGenerator.class);
        Map generatorMap = Collections.singletonMap(ConsoleAppenderDefinition.class, generator);
        factory.setAppenderGenerators(generatorMap);

        builder = EasyMock.createMock(AppenderBuilder.class);
        Map builderMap = Collections.singletonMap(PhysicalConsoleAppenderDefinition.class, builder);
        factory.setAppenderBuilders(builderMap);
    }

}
