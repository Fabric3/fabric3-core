/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.introspection.xml.Writer;

/**
 * @version $Rev$ $Date$
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
