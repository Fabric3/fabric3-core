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

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 *
 */
@EagerInit
public class TemplateLoaderTestCase extends TestCase {
    private static final String XML = "<binding.template name='template'/>";
    private static final String XML_NO_NAME = "<binding.template/>";

    private TemplateRegistry registry;
    private TemplateLoader loader;
    private IntrospectionContext context;
    private MockXMLFactory factory;


    public void testLoad() throws Exception {
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ModelObject modelObject = new ModelObject() {
        };
        EasyMock.expect(registry.resolve(EasyMock.eq(ModelObject.class), EasyMock.eq("template"))).andReturn(modelObject);
        EasyMock.replay(registry);

        loader.load(reader, context);
        EasyMock.verify(registry);
        assertFalse(context.hasErrors());
    }

    public void testNotFound() throws Exception {
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        EasyMock.expect(registry.resolve(EasyMock.eq(ModelObject.class), EasyMock.eq("template"))).andReturn(null);
        EasyMock.replay(registry);

        loader.load(reader, context);
        EasyMock.verify(registry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof TemplateNotFound);
    }

    public void testNoName() throws Exception {
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        reader.nextTag();
        EasyMock.replay(registry);

        loader.load(reader, context);
        EasyMock.verify(registry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(TemplateRegistry.class);
        loader = new TemplateLoader(registry, ModelObject.class.getName());
        factory = new MockXMLFactory();
        context = new DefaultIntrospectionContext();
    }

}
