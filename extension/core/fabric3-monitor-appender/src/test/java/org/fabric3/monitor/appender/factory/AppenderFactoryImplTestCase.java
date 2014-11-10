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
package org.fabric3.monitor.appender.factory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.monitor.appender.console.ConsoleAppenderDefinition;
import org.fabric3.monitor.appender.console.PhysicalConsoleAppenderDefinition;
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
