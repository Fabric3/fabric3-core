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
import org.easymock.IAnswer;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
@EagerInit
public class TemplatesElementLoaderTestCase extends TestCase {
    private static final String XML = "<templates><template name='template'><binding/></template></templates>";

    private IntrospectionContext context;
    private MockXMLFactory factory;
    private LoaderRegistry loaderRegistry;
    private TemplatesElementLoader loader;


    @SuppressWarnings({"serial"})
    public void testLoad() throws Exception {
        final XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        final ModelObject modelObject = new ModelObject() {
        };
        EasyMock.expect(loaderRegistry.load(reader, ModelObject.class, context)).andStubAnswer(new IAnswer<ModelObject>() {
            public ModelObject answer() throws Throwable {
                reader.nextTag();
                return modelObject;
            }
        });

        EasyMock.replay(loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(loaderRegistry);
        assertFalse(context.hasErrors());
        assertEquals("templates", reader.getName().getLocalPart());
    }


    protected void setUp() throws Exception {
        super.setUp();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new TemplatesElementLoader(loaderRegistry);
        factory = new MockXMLFactory();
        context = new DefaultIntrospectionContext();
    }

}
