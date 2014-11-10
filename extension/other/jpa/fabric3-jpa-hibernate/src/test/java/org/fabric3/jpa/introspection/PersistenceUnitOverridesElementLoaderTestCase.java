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

import java.io.ByteArrayInputStream;
import java.net.URI;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.jpa.override.OverrideRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 *
 */
@EagerInit
public class PersistenceUnitOverridesElementLoaderTestCase extends TestCase {
    private static final URI CONTRIBUTION_URI = URI.create("test");

    private static final String XML = "<persistenceUnit name='unit'><property name='foo' vaue='bar'/></persistenceUnit>";
    private static final String XML_NO_NAME = "<persistenceUnit><property name='foo' vaue='bar'/></persistenceUnit>";

    private OverrideRegistry overridesRegistry;
    private PersistenceUnitOverridesElementLoader loader;
    private IntrospectionContext context;
    private LoaderRegistry loaderRegistry;


    @SuppressWarnings({"serial"})
    public void testLoad() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        overridesRegistry.register(EasyMock.eq(CONTRIBUTION_URI), EasyMock.isA(PersistenceOverrides.class));

        EasyMock.replay(overridesRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(overridesRegistry, loaderRegistry);
        assertFalse(context.hasErrors());
    }

    public void testNoName() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        reader.nextTag();

        EasyMock.replay(overridesRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(overridesRegistry, loaderRegistry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    protected void setUp() throws Exception {
        super.setUp();
        overridesRegistry = EasyMock.createMock(OverrideRegistry.class);
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new PersistenceUnitOverridesElementLoader(loaderRegistry, overridesRegistry);
        context = new DefaultIntrospectionContext(CONTRIBUTION_URI, null, null, null);
    }

}
