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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.introspection.xml.Writer;

/**
 *
 */
public class CompositeWriterTestCase extends TestCase {

    private String XML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'\n" +
            "           xmlns:f3='urn:fabric3.org'\n" +
            "           targetNamespace='http://example.com' " +
            "           name='composite'>\n" +
            "    <!-- comments -->\n" +
            "<component name='FooComponent'/>" +
            "</composite>";

    private XMLStreamReader reader;
    private CompositeWriter compositeWriter;
    private XMLOutputFactory outputFactory;
    private Composite composite;

    public void testRoundTrip() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stream);
        compositeWriter.write(composite, xmlWriter);

    }

    protected void setUp() throws Exception {
        super.setUp();
        XMLInputFactory factory = XMLInputFactory.newInstance();

        composite = new Composite(new QName("foo", "bar"));
        composite.enableRoundTrip();
        composite.addNamespace(null, org.oasisopen.sca.Constants.SCA_NS);
        composite.addComment("This is a comment");
        ComponentDefinition<?> component = new ComponentDefinition("component");
        composite.add(component);

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
