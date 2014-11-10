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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.util;

import java.net.URI;

import junit.framework.TestCase;

/**
 *
 */
public class URIHelperTestCase extends TestCase {

    public void testBaseName() throws Exception {
        URI uri = new URI("foo");
        assertEquals("foo", UriHelper.getBaseName(uri));
    }

    public void testBaseNameScheme() throws Exception {
        URI uri = new URI("sca://foo");
        assertEquals("foo", UriHelper.getBaseName(uri));
    }

    public void testBaseNameSchemePath() throws Exception {
        URI uri = new URI("sca://bar/foo");
        assertEquals("foo", UriHelper.getBaseName(uri));
    }

    public void testBaseNamePath() throws Exception {
        URI uri = new URI("bar/foo");
        assertEquals("foo", UriHelper.getBaseName(uri));
    }

    public void testBaseNameFragment() throws Exception {
        URI uri = new URI("#foo");
        assertEquals("#foo", UriHelper.getBaseName(uri));
    }

    public void testParentName() throws Exception {
        URI uri = new URI("fabric3://grandparent/parent/child");
        assertEquals("fabric3://grandparent/parent", UriHelper.getParentName(uri));
    }

    public void testDefragmentedNameScheme() throws Exception {
        URI uri = new URI("sca://foo/bar#bar");
        assertEquals("sca://foo/bar", UriHelper.getDefragmentedName(uri).toString());
    }

    public void testDefragmentedName() throws Exception {
        URI uri = new URI("foo/bar#bar");
        assertEquals("foo/bar", UriHelper.getDefragmentedName(uri).toString());
    }

    public void testDefragmentedNoName() throws Exception {
        URI uri = new URI("#bar");
        assertEquals("", UriHelper.getDefragmentedName(uri).toString());
    }

}
