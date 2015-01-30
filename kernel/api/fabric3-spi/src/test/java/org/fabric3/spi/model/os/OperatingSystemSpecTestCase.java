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
package org.fabric3.spi.model.os;

import junit.framework.TestCase;
import org.fabric3.api.host.Version;
import org.fabric3.api.host.os.OperatingSystem;

/**
 *
 */
public class OperatingSystemSpecTestCase extends TestCase {

    public void testMatchSpecificVersionOS() throws Exception {
        Version version = new Version(1, 2, 3, "alpha");
        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", version, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", version);
        assertTrue(os1.matches(os2));
    }



    public void testMatchSpecificVersion() throws Exception {
        Version version = new Version(1, 2, 3, "alpha");
        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", version, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", version);
        assertTrue(os1.matches(os2));
    }

        public void testMatchRangeVersionOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(1, 5, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMinExclusiveOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(1, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, false);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertFalse(os1.matches(os2));
    }

    /**
     * Versions specified without a range default to minimum inclusive with a maximum to infinity
     */
    public void testMatchVersionOSGiDefaultOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMinInclusiveOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(1, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMaxExclusiveOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, false);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertFalse(os1.matches(os2));
    }

    public void testMatchRangeVersionMaxInclusiveOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertTrue(os1.matches(os2));
    }


    public void testOutOfMinRangeOS() throws Exception {
        Version min = new Version(1, 6, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(1, 5, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertFalse(os1.matches(os2));
    }

    public void testOutOfMaxRangeOS() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 5, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion);
        assertFalse(os1.matches(os2));
    }

    public void testMatchRangeVersion() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(1, 5, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMinExclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(1, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, false);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

    /**
     * Versions specified without a range default to minimum inclusive with a maximum to infinity
     */
    public void testMatchVersionOSGiDefault() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMinInclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(1, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMaxExclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, false);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

    public void testMatchRangeVersionMaxInclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }


    public void testOutOfMinRange() throws Exception {
        Version min = new Version(1, 6, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(1, 5, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

    public void testOutOfMaxRange() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 5, 0);

        OperatingSystemSpec os1 = new OperatingSystemSpec("os", "64", min, true, max, true);
        OperatingSystemSpec os2 = new OperatingSystemSpec("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

}
