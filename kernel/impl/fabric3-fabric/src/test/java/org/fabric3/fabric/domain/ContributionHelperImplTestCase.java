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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 *
 */
public class ContributionHelperImplTestCase extends TestCase {
    private ContributionHelperImpl helper;
    private MetaDataStore store;
    private HostInfo info;

    public void testFindComposite() throws Exception {
        QName name = new QName("foo", "bar");
        Composite composite = new Composite(name);
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        EasyMock.expect(store.find(Composite.class, symbol)).andReturn(element);

        EasyMock.replay(store);

        Composite returned = helper.findComposite(name);
        assertEquals(composite, returned);

        EasyMock.verify(store);
    }

    public void testFindContributions() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        EasyMock.expect(store.find(uri)).andReturn(contribution);

        EasyMock.replay(store);

        Set<Contribution> returned = helper.findContributions(Collections.singletonList(uri));
        assertEquals(contribution, returned.iterator().next());

        EasyMock.verify(store);
    }

    public void testDefaultPlan() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);

        QName name = new QName("foo", "plan");
        QNameSymbol symbol = new QNameSymbol(name);
        DeploymentPlan plan = new DeploymentPlan("plan");
        ResourceElement<QNameSymbol, DeploymentPlan> element = new ResourceElement<>(symbol, plan);

        Resource resource = new Resource(contribution, null, "");
        resource.addResourceElement(element);
        contribution.addResource(resource);
        EasyMock.replay(store);

        DeploymentPlan returned = helper.findDefaultPlan(contribution);
        assertEquals(plan, returned);
        EasyMock.verify(store);
    }

    public void testFindPlan() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);

        QName name = new QName("foo", "plan");
        QNameSymbol symbol = new QNameSymbol(name);
        DeploymentPlan plan = new DeploymentPlan("plan");
        ResourceElement<QNameSymbol, DeploymentPlan> element = new ResourceElement<>(symbol, plan);

        Resource resource = new Resource(contribution, null, "");
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(EasyMock.eq(DeploymentPlan.class), EasyMock.isA(QNameSymbol.class))).andReturn(element);
        EasyMock.replay(store);

        DeploymentPlan returned = helper.findPlan("plan");
        assertEquals(plan, returned);
        EasyMock.verify(store);
    }

    public void testGetDeployables() throws Exception {
        URI uri = URI.create("test");
        QName name1 = new QName("foo", "bar");
        QName name2 = new QName("foo", "bar2");
        Contribution contribution = new Contribution(uri);

        Composite composite1 = addComposite(name1, contribution);
        Composite composite2 = addComposite(name2, contribution);

        contribution.getManifest().addDeployable(new Deployable(name1, Collections.singletonList(RuntimeMode.VM), Collections.<String>emptyList()));
        contribution.getManifest().addDeployable(new Deployable(name2,
                                                                Collections.singletonList(RuntimeMode.PARTICIPANT),
                                                                Collections.<String>emptyList()));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();

        EasyMock.replay(store, info);

        List<Composite> returned = helper.getDeployables(Collections.singleton(contribution));
        assertEquals(1, returned.size());
        assertTrue(returned.contains(composite1));
        assertFalse(returned.contains(composite2));

        EasyMock.verify(store, info);

    }

    public void testGetDeployablesWithEnvironment() throws Exception {
        URI uri = URI.create("test");
        QName name1 = new QName("foo", "bar");
        QName name2 = new QName("foo", "bar2");
        QName name3 = new QName("foo", "bar3");
        Contribution contribution = new Contribution(uri);

        Composite composite1 = addComposite(name1, contribution);
        Composite composite2 = addComposite(name2, contribution);
        Composite composite3 = addComposite(name3, contribution);

        contribution.getManifest().addDeployable(new Deployable(name1,
                                                                Collections.singletonList(RuntimeMode.VM),
                                                                Collections.singletonList("production")));
        contribution.getManifest().addDeployable(new Deployable(name2,
                                                                Collections.singletonList(RuntimeMode.VM),
                                                                Collections.<String>emptyList()));
        contribution.getManifest().addDeployable(new Deployable(name3,
                                                                Collections.singletonList(RuntimeMode.VM),
                                                                Collections.singletonList("test")));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();
        EasyMock.expect(info.getEnvironment()).andReturn("production").atLeastOnce();

        EasyMock.replay(store, info);

        List<Composite> returned = helper.getDeployables(Collections.singleton(contribution));
        assertEquals(2, returned.size());
        assertTrue(returned.contains(composite1));
        assertTrue(returned.contains(composite2));
        assertFalse(returned.contains(composite3));

        EasyMock.verify(store, info);

    }

    public void testLocks() throws Exception {
        URI uri = URI.create("test");
        QName name1 = new QName("foo", "bar");
        QName name2 = new QName("foo", "bar2");
        Contribution contribution = new Contribution(uri);
        contribution.getManifest().addDeployable(new Deployable(name1, Collections.singletonList(RuntimeMode.VM), Collections.<String>emptyList()));
        contribution.getManifest().addDeployable(new Deployable(name2, Collections.singletonList(RuntimeMode.VM), Collections.<String>emptyList()));
        helper.lock(Collections.singleton(contribution));
        assertTrue(contribution.isLocked());
        assertTrue(contribution.getLockOwners().contains(name1));
        assertTrue(contribution.getLockOwners().contains(name2));
    }

    private Composite addComposite(QName name, Contribution contribution) {
        Composite composite = new Composite(name);
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        Resource resource = new Resource(contribution, null, "");
        resource.addResourceElement(element);
        contribution.addResource(resource);
        return composite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = EasyMock.createMock(MetaDataStore.class);
        info = EasyMock.createMock(HostInfo.class);
        helper = new ContributionHelperImpl(store, info);
    }
}
