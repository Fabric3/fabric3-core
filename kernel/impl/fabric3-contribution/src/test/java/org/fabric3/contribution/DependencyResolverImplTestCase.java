/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;

/**
 * @version $Rev$ $Date$
 */
public class DependencyResolverImplTestCase extends TestCase {
    private static final URI CONTRIBUTION1_URI = URI.create("contribution1");
    private static final URI CONTRIBUTION2_URI = URI.create("contribution2");
    private static final URI CONTRIBUTION3_URI = URI.create("contribution3");

    private DependencyResolverImpl service;

    private Contribution contribution1;
    private Contribution contribution2;
    private Contribution contribution3;
    private MetaDataStore store;

    public void testOrder() throws Exception {
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution2);
        contributions.add(contribution1);
        contributions.add(contribution3);

        List<Contribution> ordered = service.resolve(contributions);

        assertEquals(contribution3, ordered.get(0));
        assertEquals(contribution2, ordered.get(1));
        assertEquals(contribution1, ordered.get(2));
        EasyMock.verify(store);
    }

    public void testResolveAlreadyInstalledImport() throws Exception {
        QNameImport imprt = new QNameImport("test", null);
        EasyMock.expect(store.resolve(CONTRIBUTION1_URI, imprt)).andReturn(Collections.singletonList(contribution2));
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution1);
        contributions.add(contribution3);

        List<Contribution> ordered = service.resolve(contributions);

        assertTrue(ordered.contains(contribution3));
        assertTrue(ordered.contains(contribution1));
        EasyMock.verify(store);
    }

    public void testErrorResolveUnInstalledImport() throws Exception {
        QNameImport imprt = new QNameImport("test", null);
        EasyMock.expect(store.resolve(CONTRIBUTION1_URI, imprt)).andReturn(Collections.singletonList(contribution2));
        EasyMock.replay(store);

        contribution2.setState(ContributionState.STORED);

        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution1);
        contributions.add(contribution3);

        try {
            service.resolve(contributions);
            fail();
        } catch (DependencyException e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testErrorResolveNoImport() throws Exception {
        QNameImport imprt = new QNameImport("test", null);
        EasyMock.expect(store.resolve(CONTRIBUTION1_URI, imprt)).andReturn(Collections.<Contribution>emptyList());
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution1);
        contributions.add(contribution3);

        try {
            service.resolve(contributions);
            fail();
        } catch (DependencyException e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testResolveAlreadyInstalledCapability() throws Exception {
        EasyMock.expect(store.resolveCapability("capability")).andReturn(Collections.singleton(contribution3));
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution1);
        contributions.add(contribution2);

        List<Contribution> ordered = service.resolve(contributions);

        assertTrue(ordered.contains(contribution2));
        assertTrue(ordered.contains(contribution1));
        EasyMock.verify(store);
    }

    public void testErrorResolveUninstalledCapability() throws Exception {
        EasyMock.expect(store.resolveCapability("capability")).andReturn(Collections.singleton(contribution3));
        EasyMock.replay(store);

        contribution3.setState(ContributionState.STORED);

        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution1);
        contributions.add(contribution2);

        try {
            service.resolve(contributions);
            fail();
        } catch (DependencyException e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testErrorResolveNoCapability() throws Exception {
        EasyMock.expect(store.resolveCapability("capability")).andReturn(Collections.<Contribution>emptySet());
        EasyMock.replay(store);


        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution1);
        contributions.add(contribution2);

        try {
            service.resolve(contributions);
            fail();
        } catch (DependencyException e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testOrderForUninstall() throws Exception {
        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution2);
        contributions.add(contribution1);

        List<Contribution> ordered = service.orderForUninstall(contributions);

        assertEquals(contribution1, ordered.get(0));
        assertEquals(contribution2, ordered.get(1));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        createContributions();
        store = EasyMock.createMock(MetaDataStore.class);
        service = new DependencyResolverImpl(store);
    }

    private void createContributions() throws MalformedURLException {
        Capability capability = new Capability("capability");

        contribution1 = new Contribution(CONTRIBUTION1_URI);
        QNameImport imprt = new QNameImport("test", null);
        contribution1.getManifest().addImport(imprt);
        contribution1.setState(ContributionState.INSTALLED);

        contribution2 = new Contribution(CONTRIBUTION2_URI);
        QNameExport export = new QNameExport("test");
        contribution2.getManifest().addExport(export);
        contribution2.getManifest().addRequiredCapability(capability);
        contribution2.setState(ContributionState.INSTALLED);

        contribution3 = new Contribution(CONTRIBUTION3_URI);
        contribution3.getManifest().addProvidedCapability(capability);
        contribution3.setState(ContributionState.INSTALLED);

        QNameContributionWire wire = new QNameContributionWire(imprt, export, CONTRIBUTION1_URI, CONTRIBUTION2_URI);
        contribution1.addWire(wire);
    }

}
