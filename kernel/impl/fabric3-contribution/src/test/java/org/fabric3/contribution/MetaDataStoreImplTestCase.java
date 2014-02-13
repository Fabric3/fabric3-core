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
package org.fabric3.contribution;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.model.type.component.Composite;
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
        Contribution otherContribution = new Contribution(otherContributionUri, null, null, 1, "application/xml", false);
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

        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators =
                new HashMap<>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);

    }

    private void createContributions() throws MalformedURLException {
        Capability capability = new Capability("capability");

        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, 1, "application/xml", false);
        URI profileUri = URI.create("profile");
        contribution.addProfile(profileUri);
        contribution.getManifest().addExtend("extension");
        contribution.getManifest().addRequiredCapability(capability);
        deployableName = new QName("test", "composite");
        createDeployable(deployableName, contribution);
        createResourceWithComposite(deployableName, contribution);

        otherContributionUri = URI.create("otherContribution");
        URL otherLocationUrl = new URL("file://test");
        otherContribution = new Contribution(otherContributionUri, null, otherLocationUrl, 1, "application/xml", false);
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
