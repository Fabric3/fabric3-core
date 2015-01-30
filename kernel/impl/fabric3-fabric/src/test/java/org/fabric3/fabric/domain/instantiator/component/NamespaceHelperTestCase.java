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
package org.fabric3.fabric.domain.instantiator.component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class NamespaceHelperTestCase extends TestCase {

    private static final String XML = "<foo xmlns:f3='urn:fabric3.org'><bar/></foo>";
    private DocumentBuilder builder;

    public void testCopyNamespaces() throws Exception {
        Document sourceDocument = builder.parse(new ByteArrayInputStream(XML.getBytes()));
        Node source = sourceDocument.getElementsByTagName("bar").item(0);
        Document targetDocument = builder.newDocument();
        Element target = targetDocument.createElement("bar");
        targetDocument.appendChild(target);
        NamespaceHelper.copyNamespaces(source, target);
        assertEquals("urn:fabric3.org", target.lookupNamespaceURI("f3"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }
}
