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
 */
package org.fabric3.api.host;

import junit.framework.TestCase;

/**
 *
 */
public class VersionTestCase extends TestCase {

    public void testParseVersion() throws Exception {
        Version version = Version.parseVersion("1.2.3.BETA");
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals(3, version.getMicro());
        assertEquals("BETA", version.getQualifier());
    }

    public void testCompareVersion() throws Exception {
        Version version = Version.parseVersion("2.1.0");
        Version version2 = Version.parseVersion("2.2.0");
        assertEquals(-1, version.compareTo(version2));
    }


    public void testEqualsVersion() throws Exception {
        Version version = Version.parseVersion("2.1.0");
        Version version2 = Version.parseVersion("2.1.0");
        assertTrue(version.equals(version2));
    }

}