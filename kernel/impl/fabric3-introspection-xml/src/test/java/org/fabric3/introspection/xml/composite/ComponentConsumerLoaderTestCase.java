/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.io.ByteArrayInputStream;
import java.net.URI;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.introspection.xml.common.ComponentConsumerLoader;
import org.fabric3.model.type.component.ComponentConsumer;
import org.fabric3.model.type.component.Property;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.xml.XMLFactory;

/**
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class ComponentConsumerLoaderTestCase extends TestCase {
    private static final String XML = "<consumer xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='consumer' source='source'/>";

    private ComponentConsumerLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    public void testLoad() throws Exception {
        ComponentConsumer consumer = loader.load(reader, ctx);
        assertEquals("consumer", consumer.getName());
        assertEquals("source", consumer.getSources().get(0).toString());
        assertFalse(ctx.hasErrors());
    }

    public void testRoundTripLoadChannel() throws Exception {
        loader.setRoundTrip(true);
        ComponentConsumer consumer = loader.load(reader, ctx);
        assertEquals("consumer", consumer.getName());
        assertEquals("source", consumer.getSources().get(0).toString());
        assertEquals("source", consumer.getSpecifiedAttributes().iterator().next());
        assertFalse(ctx.hasErrors());
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry registry = new LoaderRegistryImpl(new MockXMLFactory());

        MockImplementationLoader implLoader = new MockImplementationLoader();
        implLoader.setProperties(new Property("prop"));
        registry.registerLoader(MockImplementation.TYPE, implLoader);
        loader = new ComponentConsumerLoader(registry);

        XMLFactory factory = new MockXMLFactory();
        reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ctx = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), null, "foo");
    }

}