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
 */
package org.fabric3.introspection.xml.writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.composite.CompositeLoader;
import org.fabric3.introspection.xml.composite.CompositeReferenceLoader;
import org.fabric3.introspection.xml.composite.CompositeServiceLoader;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.Writer;

/**
 *
 */
public class WriterRoundTripTestCase extends TestCase {

    private String XML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'\n" +
            "           targetNamespace='http://example.com' name='composite'>\n" +
            "    <service name=\"foo\" promote='component'/>\n\n" +
            "       <reference name='ref' promote='component'/>\n" +
            "    <!-- this is a test-->\n" +
            "</composite>";

    private CompositeLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext context;
    private CompositeWriter compositeWriter;
    private XMLOutputFactory outputFactory;

    public void testRoundTrip() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stream);

        loader.setRoundTrip(true);
        Composite composite = loader.load(reader, context);

        compositeWriter.write(composite, xmlWriter);


    }

    protected void setUp() throws Exception {
        super.setUp();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        CompositeReferenceLoader referenceLoader = new CompositeReferenceLoader(null, loaderHelper);
        CompositeServiceLoader serviceLoader = new CompositeServiceLoader(null, loaderHelper);
        loader = new CompositeLoader(null, serviceLoader, referenceLoader, null, null, loaderHelper);

        Writer writer = new WriterImpl();

        ComponentWriter componentWriter = new ComponentWriter(writer);
        componentWriter.init();
        CompositeReferenceWriter compositeReferenceWriter = new CompositeReferenceWriter(writer);
        compositeReferenceWriter.init();
        CompositeServiceWriter compositeServiceWriter = new CompositeServiceWriter(writer);
        compositeServiceWriter.init();
        CommentWriter commentWriter = new CommentWriter(writer);
        commentWriter.init();
        TextWriter textWriter = new TextWriter(writer);
        textWriter.init();

        compositeWriter = new CompositeWriter(writer);
        compositeWriter.init();

        outputFactory = XMLOutputFactory.newInstance();

        reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();

    }

}
