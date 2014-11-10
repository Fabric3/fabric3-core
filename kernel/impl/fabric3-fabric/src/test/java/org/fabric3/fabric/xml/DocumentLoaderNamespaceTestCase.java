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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *
 */
public class DocumentLoaderNamespaceTestCase extends TestCase {
    private static final String XML = "<?xml version='1.0' encoding='ASCII'?>\n<test><child/></test>";

    public void testLoadFromInputSource() throws Exception {
        DocumentLoaderImpl loader = new DocumentLoaderImpl();
        ByteArrayInputStream stream = new ByteArrayInputStream(XML.getBytes());
        InputSource source = new InputSource(stream);
        Document document = loader.load(source, true);
        Element root = document.getDocumentElement();
        loader.addNamespace(document, root, org.fabric3.api.Namespaces.F3);
        assertEquals(org.fabric3.api.Namespaces.F3, root.getNamespaceURI());
        Node child = root.getChildNodes().item(0);
        assertEquals(org.fabric3.api.Namespaces.F3, child.getNamespaceURI());

    }

}
