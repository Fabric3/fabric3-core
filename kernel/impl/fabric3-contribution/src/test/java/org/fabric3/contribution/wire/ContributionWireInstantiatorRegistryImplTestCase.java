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
