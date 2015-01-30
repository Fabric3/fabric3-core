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
import org.fabric3.api.host.Version;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 *
 */
public class JavaImportLoaderTestCase extends TestCase {
    private static final String XML_VERSION = "<import.java package=\"org.bar\" version=\"1.0.0\" required=\"true\"/>";
    private static final String XML_RANGE =
            "<import.java package=\"org.bar\" min=\"1.0.0\" minInclusive=\"false\" max=\"2.0.0\" maxInclusive=\"true\" required=\"true\"/>";
    private static final Version MIN_VERSION = new Version(1, 0, 0);
    private static final Version MAX_VERSION = new Version(2, 0, 0);

    private JavaImportLoader loader;
    private XMLStreamReader reader;

    public void testReadVersion() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(XML_VERSION.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        JavaImport jimport = loader.load(reader, null);
        PackageInfo info = jimport.getPackageInfo();
        assertEquals("org.bar", info.getName());
        assertEquals(0, info.getMinVersion().compareTo(MIN_VERSION));
        assertTrue(info.isRequired());
    }

    public void testReadVersionRange() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(XML_RANGE.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        JavaImport jimport = loader.load(reader, null);
        PackageInfo info = jimport.getPackageInfo();
        assertEquals("org.bar", info.getName());
        assertEquals(0, info.getMinVersion().compareTo(MIN_VERSION));
        assertFalse(info.isMinInclusive());
        assertEquals(0, info.getMaxVersion().compareTo(MAX_VERSION));
        assertTrue(info.isMaxInclusive());
        assertTrue(info.isRequired());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new JavaImportLoader();
    }


}
