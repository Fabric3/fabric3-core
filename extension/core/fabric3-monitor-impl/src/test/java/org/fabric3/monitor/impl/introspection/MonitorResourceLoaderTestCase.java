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
package org.fabric3.monitor.impl.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.monitor.spi.model.type.MonitorResource;
import org.fabric3.monitor.spi.model.type.AppenderDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class MonitorResourceLoaderTestCase extends TestCase {
    private static final String XML = "<monitor name='test'><appenders><appender.console/></appenders></monitor>";
    private static final String XML_NO_NAME = "<monitor></monitor>";
    private static final String XML_MULTIPLE_TYPES = "<monitor name='test'><appenders><appender.console/><appender.console/></appenders></monitor>";

    private LoaderRegistry loaderRegistry;
    private MonitorResourceLoader loader;

    public void testLoad() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        IntrospectionContext context = new DefaultIntrospectionContext();

        loaderRegistry.load(reader, ModelObject.class, context);
        EasyMock.expectLastCall().andReturn(new AppenderDefinition("test"));

        EasyMock.replay(loaderRegistry);
        reader.nextTag();

        MonitorResource definition = loader.load(reader, context);

        assertFalse(context.hasErrors());
        assertNotNull(definition.getDestinationDefinition());

        EasyMock.verify(loaderRegistry);
    }

    public void testNoNameLoad() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        IntrospectionContext context = new DefaultIntrospectionContext();

        EasyMock.replay(loaderRegistry);
        reader.nextTag();

        loader.load(reader, context);

        assertTrue(context.hasErrors());

        EasyMock.verify(loaderRegistry);
    }

    public void testMultipleTypesErrorLoad() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_MULTIPLE_TYPES.getBytes()));
        IntrospectionContext context = new DefaultIntrospectionContext();

        loaderRegistry.load(reader, ModelObject.class, context);
        EasyMock.expectLastCall().andReturn(new AppenderDefinition("test")).times(2);

        EasyMock.replay(loaderRegistry);
        reader.nextTag();

        loader.load(reader, context);

        assertTrue(context.hasErrors());

        EasyMock.verify(loaderRegistry);
    }

    public void setUp() throws Exception {
        super.setUp();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new MonitorResourceLoader(loaderRegistry);
    }

}
