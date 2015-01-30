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
package org.fabric3.spi.contribution.manifest;

import junit.framework.TestCase;
import org.fabric3.api.host.Version;

/**
 *
 */
public class PackageInfoTestCase extends TestCase {

    public void testMatchNameWildCard() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foo.bar.*");
        assertTrue(imprt.matches(export));
    }

    public void testMatchSubPackage() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.sub");
        PackageInfo export = new PackageInfo("foo.bar");
        assertFalse(imprt.matches(export));
    }

    public void testMatchExportedSubPackage() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar");
        PackageInfo export = new PackageInfo("foo.bar.sub");
        assertFalse(imprt.matches(export));
    }

    public void testMatchNameSecondLevelWildCard() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foo.*");
        assertTrue(imprt.matches(export));
    }

    public void testNoMatchName() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foodbardBaz");
        assertFalse(imprt.matches(export));
    }

    public void testMatchSubpackageName() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.bar.Baz");
        PackageInfo export = new PackageInfo("foo.bar.baz.Baz");
        assertFalse(imprt.matches(export));
    }

    public void testMatchSpecificVersion() throws Exception {
        Version version = new Version(1, 2, 3, "alpha");
        PackageInfo imprt = new PackageInfo("foo.bar.Baz", version, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", version, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchWildCardImport() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.*");
        PackageInfo export = new PackageInfo("foo.*");
        assertTrue(imprt.matches(export));
    }

    public void testNoMatchWildCardImport() throws Exception {
        PackageInfo imprt = new PackageInfo("foo.*");
        PackageInfo export = new PackageInfo("foo.bar.Baz");
        assertFalse(imprt.matches(export));
    }

    public void testMatchRangeVersion() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version exportedVersion = new Version(1, 5, 0);

        PackageInfo imprt = new PackageInfo("foo.bar.Baz", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchRangeVersionMinExclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version exportedVersion = new Version(1, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, false, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    /**
     * Versions specified without a range default to minimum inclusive with a maximum to infinity
     */
    public void testMatchVersionOSGiDefault() throws Exception {
        Version min = new Version(1, 0, 0);
        Version exportedVersion = new Version(2, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchRangeVersionMinInclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version exportedVersion = new Version(1, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }

    public void testMatchRangeVersionMaxExclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version exportedVersion = new Version(2, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, max, false, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    public void testMatchRangeVersionMaxInclusive() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version exportedVersion = new Version(2, 0, 0);

        PackageInfo imprt = new PackageInfo("foo.bar", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar", exportedVersion, true, true);
        assertTrue(imprt.matches(export));
    }


    public void testOutOfMinRange() throws Exception {
        Version min = new Version(1, 6, 0);
        Version max = new Version(2, 0, 0);
        Version exportedVersion = new Version(1, 5, 0);

        PackageInfo imprt = new PackageInfo("foo.bar.Baz", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    public void testOutOfMaxRange() throws Exception {
        Version min = new Version(1, 0, 0);
        Version max = new Version(2, 0, 0);
        Version exportedVersion = new Version(2, 5, 0);

        PackageInfo imprt = new PackageInfo("foo.bar.Baz", min, true, max, true, true);
        PackageInfo export = new PackageInfo("foo.bar.*", exportedVersion, true, true);
        assertFalse(imprt.matches(export));
    }

    public void testMatchWildCardExport() throws Exception {
        PackageInfo imprt = new PackageInfo("org.fabric3.api");
        PackageInfo export = new PackageInfo("org.fabric3.api.*");
        assertTrue(imprt.matches(export));
    }

    public void testMatchWildCardExportReverse() throws Exception {
        PackageInfo imprt = new PackageInfo("javax.jms.*");
        imprt.setMinVersion(new Version("1.1.1"));
        PackageInfo export = new PackageInfo("javax.jms");
        export.setMinVersion(new Version("1.1.1"));
        assertTrue(imprt.matches(export));
    }

    public void testMatchWildCardExportToNonWildcard() throws Exception {
        PackageInfo imprt = new PackageInfo("javax.jms");
        imprt.setMinVersion(new Version("1.1.1"));
        PackageInfo export = new PackageInfo("javax.jms.*");
        export.setMinVersion(new Version("1.1.1"));
        assertTrue(imprt.matches(export));
    }

    public void testMatchWildCardExportReverseNoMatch() throws Exception {
        PackageInfo imprt = new PackageInfo("javax.xml.ws.*");
        PackageInfo export = new PackageInfo("javax.xml");
        assertFalse(imprt.matches(export));
    }

    public void testMatchExportReverse() throws Exception {
        PackageInfo imprt = new PackageInfo("javax.xml.bind.annotation.adaptors");
        PackageInfo export = new PackageInfo("javax.xml.bind.*");
        assertTrue(imprt.matches(export));
    }

}
