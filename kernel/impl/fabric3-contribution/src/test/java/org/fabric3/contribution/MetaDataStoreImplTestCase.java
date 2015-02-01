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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 *
 */
public class MetaDataStoreImplTestCase extends TestCase {
    private MetaDataStoreImpl store;
    private URI contributionUri;
    private URI otherContributionUri;
    private Contribution contribution;
    private QName deployableName;
    private QName otherDeployableName;

    private Contribution otherContribution;

    public void testStoreAndFind() throws Exception {
        store.store(contribution);
        assertNotNull(store.find(contributionUri));
        assertTrue(store.getContributions().contains(contribution));
    }

    public void testGetContributions() throws Exception {
        store.store(contribution);
        assertTrue(store.getContributions().contains(contribution));
    }

    public void testRemove() throws Exception {
        store.store(contribution);
        store.remove(contributionUri);
        assertNull(store.find(contributionUri));
    }

    public void testFindBySymbol() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        QNameSymbol symbol = new QNameSymbol(deployableName);
        ResourceElement<QNameSymbol, Composite> element = store.find(Composite.class, symbol);
        assertEquals(deployableName, element.getValue().getName());
    }

    public void testFindBySymbolUri() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        QNameSymbol symbol = new QNameSymbol(deployableName);
        ResourceElement<QNameSymbol, Composite> element = store.find(contributionUri, Composite.class, symbol);
        assertEquals(deployableName, element.getValue().getName());
    }

    public void testFindBySymbolUriNotFound() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        Contribution otherContribution = new Contribution(otherContributionUri, null, null, 1, "application/xml");
        store.store(otherContribution);
        QNameSymbol symbol = new QNameSymbol(deployableName);
        assertNull(store.find(otherContributionUri, Composite.class, symbol));
    }

    public void testFindBySymbolUriResolveViaWire() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        QNameSymbol symbol = new QNameSymbol(otherDeployableName);
        ResourceElement<QNameSymbol, Composite> element = store.find(contributionUri, Composite.class, symbol);
        assertEquals(otherDeployableName, element.getValue().getName());
    }

    public void testFindByImport() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        Import imprt = new QNameImport("test", null);
        URI uri = URI.create("SomeContribution");
        assertTrue(store.resolve(uri, imprt).contains(otherContribution));
    }

    public void testResolveExtensionPoints() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        List<Contribution> list = store.resolveExtensionPoints("extension");
        assertEquals(1, list.size());
        assertTrue(list.contains(otherContribution));
    }

    public void testResolveExtensionProviders() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        assertTrue(store.resolveExtensionProviders("extension").contains(contribution));
    }

    public void testResolveCapabilities() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        Set<Contribution> set = store.resolveCapabilities(contribution);
        assertEquals(1, set.size());
        assertTrue(set.contains(otherContribution));
    }

    public void testResolveCapability() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        Set<Contribution> set = store.resolveCapability("capability");
        assertEquals(1, set.size());
        assertTrue(set.contains(otherContribution));
    }

    protected void setUp() throws Exception {
        super.setUp();
        ProcessorRegistry processorRegistry = EasyMock.createMock(ProcessorRegistry.class);

        store = new MetaDataStoreImpl(processorRegistry);

        createContributions();

        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators = new HashMap<>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);

    }

    private void createContributions() throws MalformedURLException {
        Capability capability = new Capability("capability");

        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, 1, "application/xml");
        contribution.getManifest().addExtend("extension");
        contribution.getManifest().addRequiredCapability(capability);
        deployableName = new QName("test", "composite");
        createDeployable(deployableName, contribution);
        createResourceWithComposite(deployableName, contribution);

        otherContributionUri = URI.create("otherContribution");
        URL otherLocationUrl = new URL("file://test");
        otherContribution = new Contribution(otherContributionUri, null, otherLocationUrl, 1, "application/xml");
        otherContribution.getManifest().addExtensionPoint("extension");
        otherContribution.getManifest().addProvidedCapability(capability);
        otherDeployableName = new QName("test", "otherComposite");
        createDeployable(otherDeployableName, otherContribution);
        createResourceWithComposite(otherDeployableName, otherContribution);

        createResourceWithComposite(deployableName, contribution);

        wireContributions();
    }

    private void createDeployable(QName name, Contribution contribution) {
        Deployable deployable = new Deployable(name);
        contribution.getManifest().addDeployable(deployable);
    }

    private void createResourceWithComposite(QName name, Contribution contribution) {
        Resource resource = new Resource(this.contribution, null, "application/xml");
        QNameSymbol symbol = new QNameSymbol(name);
        Composite composite = new Composite(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
    }

    private void wireContributions() {
        QNameImport imprt = new QNameImport("test", null);
        contribution.getManifest().addImport(imprt);
        QNameExport export = new QNameExport("test");
        otherContribution.getManifest().addExport(export);

        QNameContributionWire wire = new QNameContributionWire(imprt, export, otherContributionUri, otherContributionUri);
        contribution.addWire(wire);
    }

}
