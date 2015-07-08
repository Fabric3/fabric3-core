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
package org.fabric3.fabric.domain.instantiator.component;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.introspection.xml.composite.StatefulNamespaceContext;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class AbstractComponentInstantiatorTestCase extends TestCase {
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private AbstractComponentInstantiator instantiator;
    private LogicalComponent<CompositeImplementation> domain;
    private Element value;
    private Document property;

    public void testSimpleProperty() throws Exception {
        PropertyValue propertyValue = new PropertyValue("property", "$domain");
        value.setTextContent("Hello World");
        Document value = instantiator.deriveValueFromXPath(propertyValue, domain, new StatefulNamespaceContext());
        Node child = value.getDocumentElement().getFirstChild().getFirstChild();
        assertEquals(Node.TEXT_NODE, child.getNodeType());
        assertEquals("Hello World", child.getTextContent());
    }

    public void testComplexProperty() throws Exception {
        PropertyValue propertyValue = new PropertyValue("property", "$domain//http/port");

        Element http = property.createElement("http");
        value.appendChild(http);
        Element port = property.createElement("port");
        http.appendChild(port);
        port.setTextContent("8080");
        Document value = instantiator.deriveValueFromXPath(propertyValue, domain, new StatefulNamespaceContext());
        Node child = value.getDocumentElement().getFirstChild().getFirstChild();
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals("port", child.getNodeName());
        assertEquals("8080", child.getTextContent());
    }

    public void testAttributeProperty() throws Exception {
        PropertyValue propertyValue = new PropertyValue("property", "$domain//http/@port");
        Element http = property.createElement("http");
        http.setAttribute("port", "8080");
        value.appendChild(http);
        Document value = instantiator.deriveValueFromXPath(propertyValue, domain, new StatefulNamespaceContext());
        Node child = value.getDocumentElement().getFirstChild().getFirstChild();
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals("port", child.getNodeName());
        assertEquals("8080", child.getTextContent());
    }

    public void testComplexPropertyWithMultipleValues() throws Exception {
        PropertyValue propertyValue = new PropertyValue("property", "$domain//http");
        Element http1 = property.createElement("http");
        this.value.appendChild(http1);
        http1.setAttribute("index", "1");
        Element http2 = property.createElement("http");
        this.value.appendChild(http2);
        http2.setAttribute("index", "2");
        Document values = instantiator.deriveValueFromXPath(propertyValue, domain, new StatefulNamespaceContext());
        Node value = values.getDocumentElement().getChildNodes().item(0);
        NodeList list = value.getChildNodes();
        assertEquals(1, list.getLength());
        assertEquals("http", list.item(0).getNodeName());
        assertEquals("1", ((Element) list.item(0)).getAttribute("index"));

        Node value2 = values.getDocumentElement().getChildNodes().item(1);
        NodeList list2 = value2.getChildNodes();
        assertEquals(1, list2.getLength());
        assertEquals("http", list2.item(0).getNodeName());
        assertEquals("2", ((Element) list2.item(0)).getAttribute("index"));
    }

    public void testUnknownVariable() {
        PropertyValue propertyValue = new PropertyValue("property", "$foo");
        Document value = null;
        try {
            value = instantiator.deriveValueFromXPath(propertyValue, domain, new StatefulNamespaceContext());
        } catch (PropertyTypeException e) {
            // Windows throws javax.xml.xpath.XPathExpressionException

        }
        assertNull(value);
    }

    protected void setUp() throws Exception {
        super.setUp();

        instantiator = new AbstractComponentInstantiator() {
        };

        domain = new LogicalComponent<>(URI.create("fabric3://domain"), null, null);
        property = FACTORY.newDocumentBuilder().newDocument();
        Element root = property.createElement("values");
        property.appendChild(root);
        value = property.createElement("value");
        root.appendChild(value);
        LogicalProperty logicalProperty = LogicalProperty.Builder.newBuilder("domain", domain).xmlValue(property).many(false).build();
        domain.setProperties(logicalProperty);
    }

}
