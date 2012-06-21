/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class OSGiManifestEntryParserTestCase extends TestCase {
    private static final String HEADER_1 = "org.fabric3.foo;resolution:=required,org.fabric3.bar;resolution:=optional,org.fabric3.baz;version\n" +
            " =\"[1.0.0, 2.0.0)\"\n";

    private static final String HEADER_2 = "org.fabric3.baz;version=1.0.0";

    private static final String HEADER_3 = "org.fabric3.baz;version=\"[1.0.0, 2.0.0]\";resolution:=required";

    private static final String HEADER_4 = "org.fabric3.baz";

    private static final String HEADER_5 = "org.fabric3.baz,org.fabric3.bar";


    public void testHeader1() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_1);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.foo", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("resolution:=required", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.bar", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("resolution:=optional", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("version=\"[1.0.0,2.0.0)\"", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }

    public void testHeader2() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_2);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("version=1.0.0", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }

    public void testHeader3() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_3);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("version=\"[1.0.0,2.0.0]\"", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PARAMETER, type);
        assertEquals("resolution:=required", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }

    public void testHeader4() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_4);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END, type);
    }


    public void testHeader5() {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(HEADER_5);
        OSGiManifestEntryParser.EventType type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.PATH, type);
        assertEquals("org.fabric3.baz", parser.getText());
        type = parser.next();
        assertEquals(OSGiManifestEntryParser.EventType.END_CLAUSE, type);
        type = parser.next();
        assertEquals("org.fabric3.bar", parser.getText());
    }

}
