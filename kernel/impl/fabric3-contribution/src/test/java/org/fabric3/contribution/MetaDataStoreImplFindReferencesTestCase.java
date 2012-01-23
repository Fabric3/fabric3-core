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
package org.fabric3.contribution;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.ReferenceIntrospector;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImplFindReferencesTestCase extends TestCase {
    private MetaDataStoreImpl store;
    private URI contributionUri;
    private URI otherContributionUri;
    private Contribution contribution;
    private QName compositeName;
    private QName otherCompositeName;

    private Contribution otherContribution;

    @SuppressWarnings({"unchecked"})
    public void testFindReferences() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        QNameSymbol symbol = new QNameSymbol(compositeName);

        ReferenceIntrospector introspector = EasyMock.createMock(ReferenceIntrospector.class);
        EasyMock.expect(introspector.references(EasyMock.isA(ResourceElement.class), EasyMock.isA(ResourceElement.class))).andReturn(true);
        EasyMock.expect(introspector.references(EasyMock.isA(ResourceElement.class), EasyMock.isA(ResourceElement.class))).andReturn(true);
        EasyMock.replay(introspector);
        Map map = Collections.singletonMap(Composite.class, introspector);
        store.setReferenceIntrospectors(map);
        Set<ResourceElement<QNameSymbol, ?>> set = store.findReferences(contributionUri, symbol);
        assertEquals(2, set.size());
        for (ResourceElement<QNameSymbol, ?> element : set) {
            Composite value = (Composite) element.getValue();
            assertTrue(value.getName().equals(compositeName) || value.getName().equals(otherCompositeName));
        }

    }

    protected void setUp() throws Exception {
        super.setUp();
        ProcessorRegistry processorRegistry = EasyMock.createMock(ProcessorRegistry.class);

        store = new MetaDataStoreImpl(processorRegistry);

        createContributions();

        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators =
                new HashMap<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);

    }

    private void createContributions() throws MalformedURLException {

        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, 1, "application/xml", false);
        URI profileUri = URI.create("profile");
        contribution.addProfile(profileUri);
        compositeName = new QName("test", "composite");
        createResourceWithComposite(compositeName, contribution);

        otherContributionUri = URI.create("otherContribution");
        URL otherLocationUrl = new URL("file://test");
        otherContribution = new Contribution(otherContributionUri, null, otherLocationUrl, 1, "application/xml", false);
        otherCompositeName = new QName("test", "otherComposite");
        createResourceWithComposite(otherCompositeName, otherContribution);
        wireContributions();
    }

    private void createResourceWithComposite(QName name, Contribution contribution) {
        Resource resource = new Resource(this.contribution, null, "application/xml");
        QNameSymbol symbol = new QNameSymbol(name);
        Composite composite = new Composite(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, composite);
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
