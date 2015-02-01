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

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 *
 */
public class MetaDataStoreImplResolveResourceElementsTestCase extends TestCase {
    private static final URI EXPORTING_URI = URI.create("test-resource");
    private static final URI IMPORTING_URI = URI.create("test-resource2");
    private static final String IMPORT_EXPORT_QNAME = "test";
    private static final String NOT_VISIBLE = "notvisible";
    private MetaDataStoreImpl store;

    public void testResolve() throws Exception {
        List<ResourceElement<?, Composite>> list = store.resolve(IMPORTING_URI, Composite.class);
        assertEquals(2, list.size());
        for (ResourceElement<?, Composite> element : list) {
            assertFalse(NOT_VISIBLE.equals(element.getValue().getName().getLocalPart()));
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        store = new MetaDataStoreImpl(null);
        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators = new HashMap<>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);

        Contribution contribution = new Contribution(EXPORTING_URI);
        ContributionManifest manifest = contribution.getManifest();
        QNameExport export = new QNameExport(IMPORT_EXPORT_QNAME);
        manifest.addExport(export);

        createComposite(contribution, IMPORT_EXPORT_QNAME, "first");
        // create non-visible composite
        createComposite(contribution, NOT_VISIBLE, NOT_VISIBLE);
        store.store(contribution);

        Contribution contribution2 = new Contribution(IMPORTING_URI);
        ContributionManifest manifest2 = contribution2.getManifest();
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME, null);
        manifest2.addImport(imprt);
        createComposite(contribution2, IMPORT_EXPORT_QNAME, "second");

        store.store(contribution2);
        imprt.addResolved(EXPORTING_URI, export);

        List<ContributionWire<?, ?>> wires = store.resolveContributionWires(IMPORTING_URI, imprt);
        for (ContributionWire<?, ?> wire : wires) {
            contribution2.addWire(wire);
        }

    }

    private void createComposite(Contribution contribution, String namespace, String name) {
        QName qName = new QName(namespace, name);
        Composite composite = new Composite(qName);
        Resource resource = new Resource(contribution, null, null);
        QNameSymbol symbol = new QNameSymbol(qName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        element.setResource(resource);
        resource.addResourceElement(element);
        contribution.addResource(resource);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("target/repository"));
    }

}
