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
package org.fabric3.transform.property;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Base class for transform tests.
 */
public abstract class BaseTransformTest extends TestCase {


    /**
     * Builds a node from an XML fragment.
     *
     * @param xml - the xml to built into a dom node
     * @return dom Node
     * @throws IOException                  if there is an error creating a stream
     * @throws ParserConfigurationException if there is an creating the parser
     * @throws SAXException                 if there is an error parsing the string
     */
    protected Node getNode(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        return builder.parse(stream).getDocumentElement();
    }
}
