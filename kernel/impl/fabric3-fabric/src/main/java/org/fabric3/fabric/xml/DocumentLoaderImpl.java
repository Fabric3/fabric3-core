/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.fabric.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.api.host.util.IOHelper;

/**
 * Default implementation that creates a new DocumentBuilder for every invocation.
 * <p/>
 * URI resolution is handled by the underlying JAXP implementation.
 */
public class DocumentLoaderImpl implements DocumentLoader {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
    }

    public Document load(URL url, boolean stripWhitespace) throws IOException, SAXException {
        InputStream stream = url.openStream();
        try {
            stream = new BufferedInputStream(stream);
            DocumentBuilder builder = getBuilder();
            Document document = builder.parse(stream);
            if (stripWhitespace) {
                stripWhitespace(document.getDocumentElement());
            }
            return document;
        } finally {
            IOHelper.closeQuietly(stream);
        }
    }

    public Document load(InputSource source, boolean stripWhitespace) throws IOException, SAXException {
        DocumentBuilder builder = getBuilder();
        Document document = builder.parse(source);
        if (stripWhitespace) {
            stripWhitespace(document.getDocumentElement());
        }
        return document;
    }

    public void addNamespace(Document document, Node node, String namespace) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }
        if ((node.getNamespaceURI() == null || "".equals(node.getNamespaceURI()))) {
            document.renameNode(node, namespace, node.getNodeName());
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            addNamespace(document, children.item(i), org.fabric3.api.Namespaces.F3);
        }
    }


    /**
     * Recursively strips whitespace nodes starting at a DOM element.
     * <p/>
     * This is necessary as <code>DocumentBuilderFactory.setIgnoringElementContentWhitespace(boolean)</code> is broken in JDK 6:
     * <p/>
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6564400
     *
     * @param element the element
     */
    public void stripWhitespace(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
                element.removeChild(child);
            } else if (child instanceof Element) {
                stripWhitespace((Element) child);
            }
        }
    }

    private DocumentBuilder getBuilder() {
        try {
            return DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

}
