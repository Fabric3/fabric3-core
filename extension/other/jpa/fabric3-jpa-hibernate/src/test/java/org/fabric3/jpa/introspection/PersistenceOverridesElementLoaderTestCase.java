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
package org.fabric3.jpa.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class PersistenceOverridesElementLoaderTestCase extends TestCase {
    private static final String XML = "<persistence><persistenceUnit name='unit'></persistenceUnit></persistence>";

    private IntrospectionContext context;
    private LoaderRegistry loaderRegistry;
    private PersistenceOverridesElementLoader loader;


    @SuppressWarnings({"serial"})
    public void testLoad() throws Exception {
        final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        EasyMock.expect(loaderRegistry.load(reader, ModelObject.class, context)).andReturn(null);

        EasyMock.replay(loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(loaderRegistry);
        assertFalse(context.hasErrors());
        assertEquals("persistence", reader.getName().getLocalPart());
    }


    protected void setUp() throws Exception {
        super.setUp();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new PersistenceOverridesElementLoader(loaderRegistry);
        context = new DefaultIntrospectionContext();
    }

}
