/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.introspection.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Target;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;

/**
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class DefaultLoaderHelperTestCase extends TestCase {

    public static final String XML = "<composite xmlns=\"http://docs.oasis-open.org/ns/opencsa/sca/200903\" " +
            "xmlns:f3-core=\"urn:fabric3.org:core\"/>";
    private XMLInputFactory xmlFactory;
    private DefaultLoaderHelper helper;

    public void testCreateQName() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("f3-core:bar", reader);
        assertEquals("urn:fabric3.org:core", qName.getNamespaceURI());
        assertEquals("bar", qName.getLocalPart());
    }

    public void testCreateQNameContext() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("bar", reader);
        assertEquals("http://docs.oasis-open.org/ns/opencsa/sca/200903", qName.getNamespaceURI());
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

    public void testComplexProperty() throws Exception {
        String xml = "<property xmlns:foo='http://foo.com'>"
                + "<foo:a>aValue</foo:a>"
                + "<foo:b>InterestingURI</foo:b>"
                + "</property>";

        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadValue(reader);
        reader.close();

        NodeList childNodes = value.getDocumentElement().getChildNodes();
        assertEquals(2, childNodes.getLength());

        Element e = (Element) childNodes.item(0);
        assertEquals("http://foo.com", e.getNamespaceURI());
        assertEquals("a", e.getLocalName());
        assertEquals("aValue", e.getTextContent());
        e = (Element) childNodes.item(1);
        assertEquals("http://foo.com", e.getNamespaceURI());
        assertEquals("b", e.getLocalName());
        assertEquals("InterestingURI", e.getTextContent());
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

    public void testultiplicityNarrow() throws Exception {
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
