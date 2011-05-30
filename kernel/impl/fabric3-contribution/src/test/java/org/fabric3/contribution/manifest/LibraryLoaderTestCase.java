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
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.contribution.Library;
import org.fabric3.spi.contribution.OperatingSystemSpec;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
@EagerInit
public class LibraryLoaderTestCase extends TestCase {
    private static final String MINIMAL = "<library path='lib/http.dll'>" +
            "    <os name='OS1'/>" +
            "</library>";

    private static final String RANGE = "<library path='lib/http.dll'>" +
            "    <os name='OS1' min='2.1' minInclusive='false' max='3.1' maxInclusive='false'/>" +
            "</library>";

    private static final String VERSION = "<library path='lib/http.dll'>" +
            "    <os name='OS1' version='2.1' processor='x64'/>" +
            "</library>";

    private static final String MULTIPLE = "<library path='lib/http.dll'>" +
            "    <os name='OS1' version='2.1' processor='x64'/>" +
            "    <os name='OS12' version='3.1' processor='x64'/>" +
            "</library>";

    private LibraryLoader loader;
    private DefaultIntrospectionContext context;

    public void testMinimalLoad() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(MINIMAL.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        Library library = loader.load(reader, context);
        assertEquals("lib/http.dll", library.getPath());
        assertEquals(1, library.getOperatingSystems().size());
        OperatingSystemSpec os = library.getOperatingSystems().get(0);
        assertEquals("OS1", os.getName());

        assertFalse(context.hasErrors());
    }

    public void testVersionLoad() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(VERSION.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        Library library = loader.load(reader, context);
        assertEquals("lib/http.dll", library.getPath());
        assertEquals(1, library.getOperatingSystems().size());
        OperatingSystemSpec os = library.getOperatingSystems().get(0);
        assertEquals("OS1", os.getName());
        assertEquals("x64", os.getProcessor());
        assertEquals(2, os.getMinVersion().getMajor());
        assertEquals(1, os.getMinVersion().getMinor());

        assertFalse(context.hasErrors());
    }

    public void testRangeLoad() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(RANGE.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        Library library = loader.load(reader, context);
        assertEquals("lib/http.dll", library.getPath());
        assertEquals(1, library.getOperatingSystems().size());
        OperatingSystemSpec os = library.getOperatingSystems().get(0);
        assertEquals(2, os.getMinVersion().getMajor());
        assertEquals(1, os.getMinVersion().getMinor());
        assertEquals(3, os.getMaxVersion().getMajor());
        assertEquals(1, os.getMaxVersion().getMinor());
        assertFalse(os.isMinInclusive());
        assertFalse(os.isMaxInclusive());

        assertFalse(context.hasErrors());
    }

    public void testMultipleLoad() throws Exception {
        ByteArrayInputStream b = new ByteArrayInputStream(MULTIPLE.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(b);
        reader.nextTag();

        Library library = loader.load(reader, context);
        assertEquals("lib/http.dll", library.getPath());
        assertEquals(2, library.getOperatingSystems().size());
        assertFalse(context.hasErrors());
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new LibraryLoader();
        context = new DefaultIntrospectionContext();
    }

}