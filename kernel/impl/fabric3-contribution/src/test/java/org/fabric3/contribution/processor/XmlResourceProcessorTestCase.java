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
package org.fabric3.contribution.processor;

import java.net.URI;
import java.net.URL;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.MockXMLFactory;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class XmlResourceProcessorTestCase extends TestCase {
    private XmlResourceProcessor processor;
    private ProcessorRegistry registry;
    private IntrospectionContext context;
    private XmlIndexerRegistry xmlIndexerRegistry;
    private XmlResourceElementLoaderRegistry elementLoaderRegistry;
    private Resource resource;

    public void testInit() throws Exception {
        registry.register(processor);
        registry.unregister("application/xml");
        EasyMock.replay(registry);

        processor.init();
        processor.destroy();
        EasyMock.verify(registry);
    }

    public void testIndex() throws Exception {
        xmlIndexerRegistry.index(EasyMock.isA(Resource.class), EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(xmlIndexerRegistry);

        processor.index(resource, context);

        EasyMock.verify(xmlIndexerRegistry);
    }

    public void testProcess() throws Exception {
        elementLoaderRegistry.load(EasyMock.isA(XMLStreamReader.class), EasyMock.isA(Resource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(elementLoaderRegistry);

        processor.process(resource, context);

        EasyMock.verify(elementLoaderRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(ProcessorRegistry.class);
        elementLoaderRegistry = EasyMock.createMock(XmlResourceElementLoaderRegistry.class);
        xmlIndexerRegistry = EasyMock.createMock(XmlIndexerRegistry.class);
        MockXMLFactory factory = new MockXMLFactory();
        processor = new XmlResourceProcessor(registry, xmlIndexerRegistry, elementLoaderRegistry, factory);
        URL file = getClass().getResource("test.composite");
        UrlSource source = new UrlSource(file);
        Contribution contribution = new Contribution(URI.create("contribution"), source, file, -1, "application/xml", false);
        resource = new Resource(contribution, source, "application/xml");
        contribution.addResource(resource);
        context = new DefaultIntrospectionContext();
    }
}
