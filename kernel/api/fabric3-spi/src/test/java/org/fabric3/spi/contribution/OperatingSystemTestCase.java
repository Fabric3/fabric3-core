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
package org.fabric3.spi.contribution;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class OperatingSystemTestCase extends TestCase {

    public void testMatchSpecificVersion() throws Exception {
        Version version = new Version(1, 2, 3, "alpha");
        OperatingSystem os1 = new OperatingSystem("os", "64", version, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", version, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersion() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(1, 5, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMinExclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(1, 0, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, false);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

    /**
     * Versions specified without a range default to minium inclusive with a maximum to infinity
     */
    public void testMatchVersionOSGiDefault() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMinInclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version os2edVersion = new Version(1, 0, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }

    public void testMatchRangeVersionMaxExclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true, max, false);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

    public void testMatchRangeVersionMaxInclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 0, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertTrue(os1.matches(os2));
    }


    public void testOutOfMinRange() throws Exception {
        Version min = new Version(1, 6, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(1, 5, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

    public void testOutOfMaxRange() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version os2edVersion = new Version(2, 5, 0);

        OperatingSystem os1 = new OperatingSystem("os", "64", min, true, max, true);
        OperatingSystem os2 = new OperatingSystem("os", "64", os2edVersion, true);
        assertFalse(os1.matches(os2));
    }

}
