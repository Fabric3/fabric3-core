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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.composite;

import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ChannelTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.xml.XMLFactory;

/**
 *
 */
public class ChannelLoaderTestCase extends TestCase {
    private static final String XML = "<channel xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='channel'/>";

    private ChannelLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    public void testLoadChannel() throws Exception {
        ChannelDefinition channel = loader.load(reader, ctx);
        assertEquals("channel", channel.getName());
        assertFalse(ctx.hasErrors());
    }

    public void testRoundTripLoadChannel() throws Exception {
        loader.setRoundTrip(true);
        ChannelDefinition channel = loader.load(reader, ctx);
        assertEquals("channel", channel.getName());
        assertFalse(ctx.hasErrors());
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry registry = new LoaderRegistryImpl(new MockXMLFactory());
        LoaderHelper helper = new DefaultLoaderHelper();
        PropertyValueLoader pvLoader = new PropertyValueLoader(registry, helper);
        pvLoader.init();

        MockImplementationLoader implLoader = new MockImplementationLoader();
        implLoader.setProperties(new Property("prop"));
        registry.registerLoader(MockImplementation.TYPE, implLoader);
        loader = new ChannelLoader(registry, helper);
        Map map = Collections.singletonMap(ChannelDefinition.DEFAULT_TYPE, new MockChannelTypeLoader());
        loader.setChannelTypeLoaders(map);

        XMLFactory factory = new MockXMLFactory();
        reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ctx = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), null, "foo");
    }

    private class MockChannelTypeLoader implements ChannelTypeLoader {
        private final String[] EMPTY = new String[0];

        public String[] getAttributes() {
            return EMPTY;
        }

        public void load(ChannelDefinition channelDefinition, XMLStreamReader reader, IntrospectionContext context) {
        }
    }

}