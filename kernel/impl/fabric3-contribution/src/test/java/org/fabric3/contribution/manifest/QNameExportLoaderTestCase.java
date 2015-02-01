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
package org.fabric3.contribution.manifest;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.contribution.manifest.QNameExport;

/**
 *
 */
public class QNameExportLoaderTestCase extends TestCase {
    private static final String QNAME = "namespace";
    private QNameExportLoader loader = new QNameExportLoader();
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        QNameExport export = loader.load(reader, null);
        assertEquals(QNAME, export.getNamespace());
    }

    protected void setUp() throws Exception {
        super.setUp();
        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getLocation()).andReturn(null).atLeastOnce();
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "namespace")).andReturn("namespace");
        EasyMock.replay(reader);
    }
}
