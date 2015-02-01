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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 *
 */
public class JavaExportLoaderTestCase extends TestCase {
    private static final String XML = "<export.java package=\"org.bar\" version=\"1.0.1\"/>";

    private JavaExportLoader loader;
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        JavaExport export = loader.load(reader, null);
        PackageInfo info = export.getPackageInfo();
        assertEquals("org.bar", info.getName());
        assertEquals(1, info.getMinVersion().getMajor());
        assertEquals(0, info.getMinVersion().getMinor());
        assertEquals(1, info.getMinVersion().getMicro());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new JavaExportLoader();
        ByteArrayInputStream b = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();
    }
}
