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

import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.api.model.type.component.Autowire;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

import static org.oasisopen.sca.Constants.SCA_NS;


/**
 *
 */
public class CompositeLoaderTestCase extends TestCase {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private String XML_NO_AUTOWIRE ="<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' " +
                        "targetNamespace='http://example.com' name='composite'></composite>";

    private String XML_AUTOWIRE ="<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' " +
                        "targetNamespace='http://example.com' name='composite' autowire='true'></composite>";

    private CompositeLoader loader;
    private QName name;
    private XMLInputFactory factory;
    private IntrospectionContext context;

    public void testLoadNameAndDefaultAutowire() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML_NO_AUTOWIRE.getBytes()));
        reader.nextTag();
        Composite type = loader.load(reader, context);
        assertEquals(name, type.getName());
        assertEquals(Autowire.INHERITED, type.getAutowire());
    }

    public void testAutowire() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML_AUTOWIRE.getBytes()));
        reader.nextTag();
        Composite type = loader.load(reader, context);
        assertEquals(Autowire.ON, type.getAutowire());
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        loader = new CompositeLoader(null, null, null, null, null, loaderHelper);
        name = new QName("http://example.com", "composite");
    }
}
