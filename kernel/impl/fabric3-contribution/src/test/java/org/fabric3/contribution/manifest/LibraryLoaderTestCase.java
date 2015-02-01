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
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.model.os.OperatingSystemSpec;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
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