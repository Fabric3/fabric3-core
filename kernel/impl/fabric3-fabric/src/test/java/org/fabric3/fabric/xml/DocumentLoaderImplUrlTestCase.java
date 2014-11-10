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
import java.io.File;

import junit.framework.TestCase;
import org.w3c.dom.Document;

import org.fabric3.api.host.util.FileHelper;

/**
 *
 */
public class DocumentLoaderImplUrlTestCase extends TestCase {
    private static final File XML_FILE = new File("DocumentLoaderImplTestCase.xml");
    private static final String XML = "<?xml version='1.0' encoding='ASCII'?>\n<test></test>";

    public void testLoadFromUrl() throws Exception {
        DocumentLoaderImpl loader = new DocumentLoaderImpl();
        Document document = loader.load(XML_FILE.toURI().toURL(), true);
        assertEquals("test", document.getDocumentElement().getNodeName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileHelper.write(new ByteArrayInputStream(XML.getBytes()), XML_FILE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.forceDelete(XML_FILE);
    }
}
