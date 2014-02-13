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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.MockXMLFactory;
import org.fabric3.api.host.stream.InputStreamSource;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;

/**
 *
 */
public class CompositeResourceProcessorTestCase extends TestCase {
    private static final String XML = "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' targetNamespace='test' name='composite'/>";
    
    private MockXMLFactory factory;
    private ProcessorRegistry registry;
    private Loader loader;
    private QName compositeName;
    private DefaultIntrospectionContext context;
    private Resource resource;
    private Contribution contribution;

    public void testIndex() throws Exception {
        EasyMock.replay(loader, registry);

        CompositeResourceProcessor processor = new CompositeResourceProcessor(registry, loader, factory);

        processor.index(resource, context);
        assertFalse(context.hasErrors());
        assertFalse(resource.getResourceElements().isEmpty());
    }

    public void testProcess() throws Exception {
        contribution.addResource(resource);
        EasyMock.expect(loader.load(EasyMock.isA(Source.class), EasyMock.eq(Composite.class), EasyMock.isA(IntrospectionContext.class)));
        EasyMock.expectLastCall().andReturn(new Composite(compositeName));

        EasyMock.replay(loader, registry);

        CompositeResourceProcessor processor = new CompositeResourceProcessor(registry, loader, factory);

        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(new QNameSymbol(compositeName));
        resource.addResourceElement(element);

        processor.process(resource, context);
        assertFalse(context.hasErrors());
    }

    public void testDuplicateComposite() throws Exception {
        contribution.addResource(resource);
        EasyMock.replay(loader, registry);

        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(new QNameSymbol(compositeName));
        Resource otherResource = new Resource(contribution, null, "application/xml");
        otherResource.addResourceElement(element);
        contribution.addResource(otherResource);

        CompositeResourceProcessor processor = new CompositeResourceProcessor(registry, loader, factory);

        processor.index(resource, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof DuplicateComposite);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new MockXMLFactory();
        registry = EasyMock.createNiceMock(ProcessorRegistry.class);
        loader = EasyMock.createMock(Loader.class);
        context = new DefaultIntrospectionContext();
        compositeName = new QName("test", "composite");

        contribution = new Contribution(URI.create("contribution"));
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        Source source = new InputStreamSource("id", stream);
        resource = new Resource(contribution, source, "application/xml");
    }
}
