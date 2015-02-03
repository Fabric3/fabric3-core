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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import org.fabric3.api.host.Fabric3Exception;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Default implementation that creates a new DocumentBuilder for every invocation.  URI resolution is handled by the underlying JAXP implementation.
 */
public class DocumentLoaderImpl implements DocumentLoader {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
    }

    public Document load(InputSource source, boolean stripWhitespace) throws Fabric3Exception {
        DocumentBuilder builder = getBuilder();
        Document document;
        try {
            document = builder.parse(source);
        } catch (SAXException | IOException e) {
            throw new Fabric3Exception(e);
        }
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
     * Recursively strips whitespace nodes starting at a DOM element.  This is necessary as <code>DocumentBuilderFactory
     * .setIgnoringElementContentWhitespace(boolean)</code> is broken in JDK 6:  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6564400
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
