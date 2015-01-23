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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.model.plan.DeploymentPlan;

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

    public void testGetDeployables() throws Exception {
        URI uri = URI.create("test");
        QName name1 = new QName("foo", "bar");
        QName name2 = new QName("foo", "bar2");
        Contribution contribution = new Contribution(uri);

        Composite composite1 = addComposite(name1, contribution);
        Composite composite2 = addComposite(name2, contribution);

        contribution.getManifest().addDeployable(new Deployable(name1, Collections.singletonList(RuntimeMode.VM), Collections.<String>emptyList()));
        contribution.getManifest().addDeployable(new Deployable(name2,
                                                                Collections.singletonList(RuntimeMode.NODE),
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
