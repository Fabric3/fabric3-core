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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
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

    private ProcessorRegistry registry;
    private Loader loader;
    private QName compositeName;
    private DefaultIntrospectionContext context;
    private Resource resource;
    private Contribution contribution;

    public void testIndex() throws Exception {
        EasyMock.replay(loader, registry);

        CompositeResourceProcessor processor = new CompositeResourceProcessor(registry, loader);

        processor.index(resource, context);
        assertFalse(context.hasErrors());
        assertFalse(resource.getResourceElements().isEmpty());
    }

    public void testProcess() throws Exception {
        contribution.addResource(resource);
        EasyMock.expect(loader.load(EasyMock.isA(Source.class), EasyMock.eq(Composite.class), EasyMock.isA(IntrospectionContext.class)));
        EasyMock.expectLastCall().andReturn(new Composite(compositeName));

        EasyMock.replay(loader, registry);

        CompositeResourceProcessor processor = new CompositeResourceProcessor(registry, loader);

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

        CompositeResourceProcessor processor = new CompositeResourceProcessor(registry, loader);

        processor.index(resource, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof DuplicateComposite);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
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
