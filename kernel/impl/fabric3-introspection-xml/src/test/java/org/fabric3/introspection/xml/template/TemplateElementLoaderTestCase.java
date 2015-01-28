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
package org.fabric3.introspection.xml.template;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TemplateRegistry;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class TemplateElementLoaderTestCase extends TestCase {
    private static final URI CONTRIBUTION_URI = URI.create("test");

    private static final String XML = "<template name='template'><binding/></template>";
    private static final String XML_NO_BODY = "<template name='template'></template>";
    private static final String XML_NO_NAME = "<binding.template/>";

    private TemplateRegistry templateRegistry;
    private TemplateElementLoader loader;
    private IntrospectionContext context;
    private LoaderRegistry loaderRegistry;


    @SuppressWarnings({"serial"})
    public void testLoad() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ModelObject modelObject = new ModelObject() {
        };
        EasyMock.expect(loaderRegistry.load(reader, ModelObject.class, context)).andReturn(modelObject);
        templateRegistry.register("template", CONTRIBUTION_URI, modelObject);

        EasyMock.replay(templateRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(templateRegistry, loaderRegistry);
        assertFalse(context.hasErrors());
    }

    public void testNoName() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        reader.nextTag();

        EasyMock.replay(templateRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(templateRegistry, loaderRegistry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    public void testNoBody() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_NO_BODY.getBytes()));
        reader.nextTag();

        EasyMock.replay(templateRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(templateRegistry, loaderRegistry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidTemplateDefinition);
    }

    protected void setUp() throws Exception {
        super.setUp();
        templateRegistry = EasyMock.createMock(TemplateRegistry.class);
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new TemplateElementLoader(loaderRegistry, templateRegistry);
        context = new DefaultIntrospectionContext(CONTRIBUTION_URI, null, null, null);
    }

}
