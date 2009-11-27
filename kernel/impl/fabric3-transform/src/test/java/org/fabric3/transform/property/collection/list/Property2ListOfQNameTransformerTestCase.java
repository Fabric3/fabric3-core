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
*/
package org.fabric3.transform.property.collection.list;

import java.io.StringReader;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @version $Rev$ $Date$
 */
public class Property2ListOfQNameTransformerTestCase extends TestCase {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;
    private static final String PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String NO_NAMESPACES_XML = PREFIX + "<test>zero, one, two</test>";
    private static final String NAMESPACES_XML = PREFIX + "<test>{ns}zero, {ns}one, {ns}two</test>";

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
    }

    private Property2ListOfQNameTransformer transformer;

    public void testNoNamespacesTransform() throws Exception {
        Element element = createTestNode(NO_NAMESPACES_XML);
        List<QName> list = transformer.transform(element, getClass().getClassLoader());
        assertEquals(3, list.size());
        assertEquals("zero", list.get(0).getLocalPart());
        assertEquals("one", list.get(1).getLocalPart());
        assertEquals("two", list.get(2).getLocalPart());
    }

    public void testNamespacesTransform() throws Exception {
        Element element = createTestNode(NAMESPACES_XML);
        List<QName> list = transformer.transform(element, getClass().getClassLoader());
        assertEquals(3, list.size());
        assertEquals("ns", list.get(0).getNamespaceURI());
        assertEquals("ns", list.get(1).getNamespaceURI());
        assertEquals("ns", list.get(2).getNamespaceURI());
    }

    private Element createTestNode(String xml) throws Exception {
        InputSource source = new InputSource(new StringReader(xml));
        Document document = DOCUMENT_FACTORY.newDocumentBuilder().parse(source);
        return document.getDocumentElement();
    }

    protected void setUp() throws Exception {
        super.setUp();
        transformer = new Property2ListOfQNameTransformer();

    }
}
