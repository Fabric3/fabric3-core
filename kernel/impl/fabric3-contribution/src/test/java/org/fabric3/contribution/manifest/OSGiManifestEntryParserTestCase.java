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

import junit.framework.TestCase;

/**
 *
 */
public class OSGiManifestEntryParserTestCase extends TestCase {
    private static final String HEADER_1 = "org.fabric3.foo;resolution:=required,org.fabric3.bar;resolution:=optional,org.fabric3.baz;version\n"
                                           + " =\"[1.0.0, 2.0.0)\"\n";

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
        parser.next();
        assertEquals("org.fabric3.bar", parser.getText());
    }

}
