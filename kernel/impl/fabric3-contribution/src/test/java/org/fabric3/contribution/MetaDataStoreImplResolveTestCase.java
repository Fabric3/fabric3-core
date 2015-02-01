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
package org.fabric3.contribution;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;

/**
 *
 */
public class MetaDataStoreImplResolveTestCase extends TestCase {
    private static final URI RESOURCE_URI = URI.create("test-resource");
    private static final URI RESOURCE_URI2 = URI.create("test-resource2");
    private static final String IMPORT_EXPORT_QNAME = "test";
    private static final String IMPORT_EXPORT_QNAME2 = "test2";
    private MetaDataStoreImpl store;
    private QNameExport export;

    public void testResolve() throws Exception {
        URI uri = URI.create("source");
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME, null);
        imprt.addResolved(RESOURCE_URI, export);
        List<ContributionWire<?, ?>> wires = store.resolveContributionWires(uri, imprt);
        assertEquals(RESOURCE_URI, wires.get(0).getExportContributionUri());
    }

    public void testAlreadyResolved() throws Exception {
        URI uri = URI.create("source");
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME, null);
        imprt.addResolved(RESOURCE_URI, export);
        List<Contribution> contributions = store.resolve(uri, imprt);
        assertEquals(1, contributions.size());
    }

    public void testResolveDependentContributions() throws Exception {
        Set<Contribution> contributions = store.resolveDependentContributions(RESOURCE_URI);
        assertEquals(RESOURCE_URI2, contributions.iterator().next().getUri());
    }

    protected void setUp() throws Exception {
        super.setUp();
        store = new MetaDataStoreImpl(null);
        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators = new HashMap<>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);
        Contribution contribution = new Contribution(RESOURCE_URI);
        ContributionManifest manifest = contribution.getManifest();
        export = new QNameExport(IMPORT_EXPORT_QNAME);
        manifest.addExport(export);
        store.store(contribution);

        Contribution contribution2 = new Contribution(RESOURCE_URI2);
        ContributionManifest manifest2 = contribution2.getManifest();
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME, null);
        manifest2.addImport(imprt);
        QNameExport export2 = new QNameExport(IMPORT_EXPORT_QNAME2);
        manifest2.addExport(export2);
        store.store(contribution2);
        imprt.addResolved(RESOURCE_URI, export);
        List<ContributionWire<?, ?>> wires = store.resolveContributionWires(RESOURCE_URI2, imprt);
        for (ContributionWire<?, ?> wire : wires) {
            contribution2.addWire(wire);
        }

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("target/repository"));
    }

}
