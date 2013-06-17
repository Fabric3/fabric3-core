/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.contribution.wire;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import org.fabric3.contribution.manifest.ContributionExport;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;

/**
 *
 */
public class ContributionWireInstantiatorRegistryImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testInstantiateJavaWire() throws Exception {
        JavaContributionWireInstantiator instantiator = new JavaContributionWireInstantiator();
        Map instantiators = Collections.singletonMap(JavaImport.class, instantiator);
        ContributionWireInstantiatorRegistryImpl registry = new ContributionWireInstantiatorRegistryImpl();
        registry.setInstantiators(instantiators);

        PackageInfo info = new PackageInfo("org.fabric3");
        JavaImport imprt = new JavaImport(info);
        JavaExport export = new JavaExport(info);

        URI importUri = URI.create("import");
        URI exportUri = URI.create("export");
        ContributionWire wire = registry.instantiate(imprt, export, importUri, exportUri);
        assertEquals(imprt, wire.getImport());
        assertEquals(export, wire.getExport());
        assertEquals(importUri, wire.getImportContributionUri());
        assertEquals(exportUri, wire.getExportContributionUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testInstantiateLocationWire() throws Exception {
        LocationContributionWireInstantiator instantiator = new LocationContributionWireInstantiator();
        Map instantiators = Collections.singletonMap(ContributionImport.class, instantiator);
        ContributionWireInstantiatorRegistryImpl registry = new ContributionWireInstantiatorRegistryImpl();
        registry.setInstantiators(instantiators);

        URI contributionUri = URI.create("contribution");
        ContributionImport imprt = new ContributionImport(contributionUri);
        ContributionExport export = new ContributionExport(contributionUri);

        URI importUri = URI.create("import");
        URI exportUri = URI.create("export");
        ContributionWire wire = registry.instantiate(imprt, export, importUri, exportUri);
        assertEquals(imprt, wire.getImport());
        assertEquals(export, wire.getExport());
        assertEquals(importUri, wire.getImportContributionUri());
        assertEquals(exportUri, wire.getExportContributionUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testInstantiateQNameWire() throws Exception {
        QNameWireInstantiator instantiator = new QNameWireInstantiator();
        Map instantiators = Collections.singletonMap(QNameImport.class, instantiator);
        ContributionWireInstantiatorRegistryImpl registry = new ContributionWireInstantiatorRegistryImpl();
        registry.setInstantiators(instantiators);

        URI contributionUri = URI.create("contribution");
        QNameImport imprt = new QNameImport("test", contributionUri);
        QNameExport export = new QNameExport("test");

        URI importUri = URI.create("import");
        URI exportUri = URI.create("export");
        ContributionWire wire = registry.instantiate(imprt, export, importUri, exportUri);
        assertEquals(imprt, wire.getImport());
        assertEquals(export, wire.getExport());
        assertEquals(importUri, wire.getImportContributionUri());
        assertEquals(exportUri, wire.getExportContributionUri());
    }

}
