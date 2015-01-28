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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.WireDefinition;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.oasisopen.sca.Constants;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 *
 */
public class WireLoaderTestCase extends TestCase {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private String XML = "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' "
            + "targetNamespace='http://example.com' name='composite'>"
            + "<wire source='source' target='target'/>"
            + "<wire source='source/reference' target='target/service'/>"
            + "<wire source='source/reference/binding' target='target/service/binding'/>"
            + "</composite>";

    private CompositeLoader loader;
    private XMLInputFactory factory;
    private IntrospectionContext context;

    public void testLoadWire() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        Composite type = loader.load(reader, context);
        assertEquals(3, type.getWires().size());
        WireDefinition wire1 = type.getWires().get(0);
        WireDefinition wire2 = type.getWires().get(1);
        WireDefinition wire3 = type.getWires().get(2);

        assertEquals("source", wire1.getReferenceTarget().getComponent());
        assertEquals("reference", wire2.getReferenceTarget().getBindable());
        assertEquals("binding", wire3.getReferenceTarget().getBinding());

        assertEquals("target", wire1.getServiceTarget().getComponent());
        assertEquals("service", wire2.getServiceTarget().getBindable());
        assertEquals("binding", wire3.getServiceTarget().getBinding());

    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        WireLoader wireLoader = new WireLoader(loaderHelper);
        LoaderRegistry registry = new LoaderRegistryImpl();
        registry.registerLoader(new QName(Constants.SCA_NS, "wire"), wireLoader);
        loader = new CompositeLoader(registry, null, null, null, null, loaderHelper);
    }
}
