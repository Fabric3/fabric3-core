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
package org.fabric3.introspection.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class DefaultLoaderHelperTestCase extends TestCase {

    public static final String XML = "<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200912\" " +
            "xmlns:f3=\"urn:fabric3.org\"/>";
    private XMLInputFactory xmlFactory;
    private DefaultLoaderHelper helper;

    public void testCreateQName() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("f3:bar", reader);
        assertEquals("urn:fabric3.org", qName.getNamespaceURI());
        assertEquals("bar", qName.getLocalPart());
    }

    public void testCreateQNameContext() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("bar", reader);
        assertEquals("http://docs.oasis-open.org/ns/opencsa/sca/200912", qName.getNamespaceURI());
    }

    public void testCreateQNameInvalidPrefix() throws Exception {
        XMLStreamReader reader = createReader(XML);
        try {
            helper.createQName("bad:bar", reader);
            fail();
        } catch (InvalidPrefixException e) {
            //expected
        }
    }

    public void testTransform() throws Exception {
        String xml = "<root><one><two>value</two></one></root>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.transform(reader);
        reader.close();
        Node root = value.getDocumentElement();
        assertEquals("root", root.getNodeName());
        Element one = (Element) value.getElementsByTagName("one").item(0);
        assertEquals("one", one.getNodeName());
        Element two = (Element) one.getChildNodes().item(0);
        assertEquals("two", two.getNodeName());
        assertEquals("value", two.getTextContent());
    }

    public void testLoadSimpleValue() throws Exception {
        String xml = "<property name='test'>value</property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Node e = value.getDocumentElement().getFirstChild();
        assertEquals("value", e.getTextContent());
    }

    public void testLoadMutlipleSimpleValue() throws Exception {
        String xml = "<property name='test'><value>value1</value><value>value2</value></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        Element values = value.getDocumentElement();
        assertEquals(2, values.getChildNodes().getLength());
        reader.close();
        Node e = values.getChildNodes().item(0).getFirstChild();
        assertEquals("value1", e.getTextContent());
        e = values.getChildNodes().item(1).getFirstChild();
        assertEquals("value2", e.getTextContent());
    }

    public void testLoadComplexGlobalElement() throws Exception {
        String xml = "<property name='test'><foo:a xmlns:foo='http://foo.com'>value</foo:a></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Node e = value.getDocumentElement().getFirstChild();
        assertEquals("http://foo.com", e.getNamespaceURI());
        assertEquals("a", e.getLocalName());
        assertEquals("value", e.getTextContent());
    }

    public void testLoadComplexValue() throws Exception {
        String xml = "<property name='test'><value><a xmlns:foo='http://foo.com'><b>value</b></a></value></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Node e = value.getDocumentElement().getFirstChild().getFirstChild().getFirstChild();
        assertEquals("value", e.getTextContent());
    }

    public void testLoadComplexValueMultipleElements() throws Exception {
        String xml = "<property name='test'><value><xml>application/xml</xml><composite>text/vnd.fabric3.composite+xml</composite>" +
                "<zip>application/zip</zip><jar>application/zip</jar></value></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        assertEquals(4, value.getDocumentElement().getFirstChild().getChildNodes().getLength());
    }

    public void testLoadMultipleComplexValue() throws Exception {
        String xml = "<property name='test'><value><a xmlns:foo='http://foo.com'><b>value1</b></a></value><value>" +
                "<a xmlns:foo='http://foo.com'><b>value2</b></a></value></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Element values = value.getDocumentElement();
        assertEquals(2, values.getChildNodes().getLength());
        Node e = values.getChildNodes().item(0).getFirstChild();
        assertEquals("value1", e.getTextContent());
        e = values.getChildNodes().item(1).getFirstChild();
        assertEquals("value2", e.getTextContent());
    }

    public void testLoadMultipleComplexGlobalElement() throws Exception {
        String xml = "<property name='p'><foo:a xmlns:foo='http://foo.com'>value1</foo:a><foo:a xmlns:foo='http://foo.com'>value2</foo:a></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Element values = value.getDocumentElement();
        assertEquals(2, values.getChildNodes().getLength());
        Node e = values.getChildNodes().item(0).getFirstChild();
        assertEquals("value1", e.getTextContent());
        e = values.getChildNodes().item(1).getFirstChild();
        assertEquals("value2", e.getTextContent());
    }

    public void testLoadMap() throws Exception {
        String xml = "<property name='p'><value><entry><key>one</key><value>one</value></entry><entry>" +
                "<key>two</key><value>two</value></entry></value></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Element root = value.getDocumentElement();
        assertEquals(1, root.getChildNodes().getLength());
        Node entry = root.getFirstChild().getChildNodes().item(0);
        Node key = entry.getChildNodes().item(0);
        assertEquals("one", key.getTextContent());
        Node val = entry.getChildNodes().item(1);
        assertEquals("one", val.getTextContent());
        Node entry2 = root.getFirstChild().getChildNodes().item(1);
        Node key2 = entry2.getChildNodes().item(0);
        assertEquals("two", key2.getTextContent());
        Node val2 = entry2.getChildNodes().item(1);
        assertEquals("two", val2.getTextContent());
    }

    public void testNestedValueTags() throws Exception {
        String xml = "<property name='p'><value><value>value</value></value></property>";
        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadPropertyValues(reader);
        reader.close();
        Element root = value.getDocumentElement();
        assertEquals(1, root.getChildNodes().getLength());
        assertEquals("value", root.getFirstChild().getTextContent());
    }

    public void testParseTargetComponent() throws Exception {
        Target target = helper.parseTarget("component", EasyMock.createNiceMock(XMLStreamReader.class));
        assertEquals("component", target.getComponent());
    }

    public void testParseTargetComponentService() throws Exception {
        Target target = helper.parseTarget("component/service", EasyMock.createNiceMock(XMLStreamReader.class));
        assertEquals("component", target.getComponent());
        assertEquals("service", target.getBindable());
    }

    public void testParseTargetComponentServiceBinding() throws Exception {
        Target target = helper.parseTarget("component/service/binding", EasyMock.createNiceMock(XMLStreamReader.class));
        assertEquals("component", target.getComponent());
        assertEquals("service", target.getBindable());
        assertEquals("binding", target.getBinding());
    }

    public void testMultiplicityNarrow() throws Exception {
        assertTrue(helper.canNarrow(Multiplicity.ONE_ONE, Multiplicity.ZERO_ONE));
        assertTrue(helper.canNarrow(Multiplicity.ONE_ONE, Multiplicity.ONE_ONE));
        assertTrue(helper.canNarrow(Multiplicity.ZERO_N, Multiplicity.ZERO_N));
        assertTrue(helper.canNarrow(Multiplicity.ZERO_ONE, Multiplicity.ZERO_N));
        assertTrue(helper.canNarrow(Multiplicity.ZERO_ONE, Multiplicity.ZERO_ONE));
        assertTrue(helper.canNarrow(Multiplicity.ONE_ONE, Multiplicity.ZERO_N));
        assertTrue(helper.canNarrow(Multiplicity.ONE_N, Multiplicity.ZERO_N));
        assertTrue(helper.canNarrow(Multiplicity.ONE_ONE, Multiplicity.ONE_N));

        assertFalse(helper.canNarrow(Multiplicity.ZERO_ONE, Multiplicity.ONE_ONE));
        assertFalse(helper.canNarrow(Multiplicity.ZERO_N, Multiplicity.ZERO_ONE));
        assertFalse(helper.canNarrow(Multiplicity.ZERO_N, Multiplicity.ONE_ONE));
        assertFalse(helper.canNarrow(Multiplicity.ZERO_N, Multiplicity.ONE_N));
        assertFalse(helper.canNarrow(Multiplicity.ONE_N, Multiplicity.ONE_ONE));
    }

    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();

        helper = new DefaultLoaderHelper();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {

        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;

    }

}
