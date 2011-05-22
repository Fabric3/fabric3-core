/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.contribution.manifest;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.contribution.Version;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * @version $Rev$ $Date$
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
