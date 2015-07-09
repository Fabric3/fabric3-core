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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;

/**
 *
 */
public class DependencyResolverImplTestCase extends TestCase {
    private static final URI CONTRIBUTION1_URI = URI.create("contribution1");
    private static final URI CONTRIBUTION2_URI = URI.create("contribution2");
    private static final URI CONTRIBUTION3_URI = URI.create("contribution3");

    private DependencyResolverImpl resolver;

    private Contribution contribution1;
    private Contribution contribution2;
    private Contribution contribution3;
    private MetaDataStore store;

    public void testOrder() throws Exception {
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution2);
        contributions.add(contribution1);
        contributions.add(contribution3);

        List<Contribution> ordered = resolver.resolve(contributions);

        assertEquals(contribution3, ordered.get(0));
        assertEquals(contribution2, ordered.get(1));
        assertEquals(contribution1, ordered.get(2));
        EasyMock.verify(store);
    }

    public void testResolveAlreadyInstalledImport() throws Exception {
        QNameImport imprt = new QNameImport("test", null);
        EasyMock.expect(store.resolve(CONTRIBUTION1_URI, imprt)).andReturn(Collections.singletonList(contribution2));
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution1);
        contributions.add(contribution3);

        List<Contribution> ordered = resolver.resolve(contributions);

        assertTrue(ordered.contains(contribution3));
        assertTrue(ordered.contains(contribution1));
        EasyMock.verify(store);
    }

    public void testErrorResolveUnInstalledImport() throws Exception {
        QNameImport imprt = new QNameImport("test", null);
        EasyMock.expect(store.resolve(CONTRIBUTION1_URI, imprt)).andReturn(Collections.singletonList(contribution2));
        EasyMock.replay(store);

        contribution2.uninstall();

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution1);
        contributions.add(contribution3);

        try {
            resolver.resolve(contributions);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testErrorResolveNoImport() throws Exception {
        QNameImport imprt = new QNameImport("test", null);
        EasyMock.expect(store.resolve(CONTRIBUTION1_URI, imprt)).andReturn(Collections.<Contribution>emptyList());
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution1);
        contributions.add(contribution3);

        try {
            resolver.resolve(contributions);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testResolveAlreadyInstalledCapability() throws Exception {
        EasyMock.expect(store.resolveCapability("capability")).andReturn(Collections.singleton(contribution3));
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution1);
        contributions.add(contribution2);

        List<Contribution> ordered = resolver.resolve(contributions);

        assertTrue(ordered.contains(contribution2));
        assertTrue(ordered.contains(contribution1));
        EasyMock.verify(store);
    }

    public void testErrorResolveUninstalledCapability() throws Exception {
        EasyMock.expect(store.resolveCapability("capability")).andReturn(Collections.singleton(contribution3));
        EasyMock.replay(store);

        contribution3.uninstall();

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution1);
        contributions.add(contribution2);

        try {
            resolver.resolve(contributions);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testErrorResolveNoCapability() throws Exception {
        EasyMock.expect(store.resolveCapability("capability")).andReturn(Collections.<Contribution>emptySet());
        EasyMock.replay(store);

        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution1);
        contributions.add(contribution2);

        try {
            resolver.resolve(contributions);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }

        EasyMock.verify(store);
    }

    public void testOrderForUninstall() throws Exception {
        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution2);
        contributions.add(contribution1);

        List<Contribution> ordered = resolver.orderForUninstall(contributions);

        assertEquals(contribution1, ordered.get(0));
        assertEquals(contribution2, ordered.get(1));
    }

    public void testOrderContributionAndDependents() throws Exception {
        PackageInfo c1 = new PackageInfo("c1");
        PackageInfo c2 = new PackageInfo("c2");
        JavaImport import1 = new JavaImport(c1);
        JavaExport export1 = new JavaExport(c1);
        JavaImport import2 = new JavaImport(c2);
        JavaExport export2 = new JavaExport(c2);

        import1.addResolved(CONTRIBUTION1_URI, export1);
        import2.addResolved(CONTRIBUTION2_URI, export2);

        export1.resolve();
        export2.resolve();

        contribution1.getManifest().addExport(export1);
        contribution2.getManifest().addImport(import1);
        contribution2.getManifest().addExport(export2);
        contribution3.getManifest().addImport(import2);

        contribution1.getWires().clear(); // clear QName wire in setup

        ContributionWire wire21 = new JavaContributionWire(import1, export1, CONTRIBUTION2_URI, CONTRIBUTION1_URI);
        contribution2.addWire(wire21);
        ContributionWire wire32 = new JavaContributionWire(import2, export2, CONTRIBUTION3_URI, CONTRIBUTION2_URI);
        contribution3.addWire(wire32);
        EasyMock.expect(store.find(CONTRIBUTION1_URI)).andReturn(contribution1);
        EasyMock.expect(store.getContributions()).andReturn(new HashSet<>(Arrays.asList(contribution1, contribution2, contribution3))).atLeastOnce();
        EasyMock.expect(store.find(CONTRIBUTION2_URI)).andReturn(contribution2);


        EasyMock.replay(store);

        List<Contribution> ordered = resolver.orderContributionAndDependents(contribution1.getUri());

        assertEquals(3, ordered.size());
        assertEquals(contribution3, ordered.get(0));
        assertEquals(contribution2, ordered.get(1));
        assertEquals(contribution1, ordered.get(2));

        ordered = resolver.orderContributionAndDependents(contribution2.getUri());
        assertEquals(2, ordered.size());
        assertEquals(contribution3, ordered.get(0));
        assertEquals(contribution2, ordered.get(1));

        EasyMock.verify(store);
    }

    protected void setUp() throws Exception {
        super.setUp();

        createContributions();
        store = EasyMock.createMock(MetaDataStore.class);
        resolver = new DependencyResolverImpl(store);
    }

    private void createContributions() {
        Capability capability = new Capability("capability");

        contribution1 = new Contribution(CONTRIBUTION1_URI);
        QNameImport imprt = new QNameImport("test", null);
        contribution1.getManifest().addImport(imprt);
        contribution1.install();

        contribution2 = new Contribution(CONTRIBUTION2_URI);
        QNameExport export = new QNameExport("test");
        contribution2.getManifest().addExport(export);
        contribution2.getManifest().addRequiredCapability(capability);
        contribution2.install();

        contribution3 = new Contribution(CONTRIBUTION3_URI);
        contribution3.getManifest().addProvidedCapability(capability);
        contribution3.install();

        QNameContributionWire wire = new QNameContributionWire(imprt, export, CONTRIBUTION1_URI, CONTRIBUTION2_URI);
        contribution1.addWire(wire);
    }

}
