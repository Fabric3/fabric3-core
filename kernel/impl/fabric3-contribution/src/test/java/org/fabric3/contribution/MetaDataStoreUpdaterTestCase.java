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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.ResourceElementUpdater;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;

/**
 *
 */
public class MetaDataStoreUpdaterTestCase extends TestCase {
    private MetaDataStoreImpl store;
    private URI contributionUri;
    private URI otherContributionUri;
    private Contribution contribution;
    private Composite composite;
    private Contribution otherContribution;

    @SuppressWarnings({"unchecked"})
    public void testUpdate() throws Exception {
        store.store(contribution);
        store.store(otherContribution);

        ResourceElementUpdater<Composite> updater = EasyMock.createMock(ResourceElementUpdater.class);
        EasyMock.expect(updater.update(EasyMock.isA(Composite.class), EasyMock.isA(Contribution.class), EasyMock.isA(Set.class)))
                .andReturn(Collections.<ModelObject>emptySet());
        EasyMock.replay(updater);
        Map map = Collections.singletonMap(Composite.class.getName(), updater);
        store.setUpdaters(map);
        store.update(contributionUri, composite);

        EasyMock.verify(updater);
    }

    @SuppressWarnings({"unchecked"})
    public void testRemove() throws Exception {
        store.store(contribution);
        store.store(otherContribution);

        ResourceElementUpdater<Composite> updater = EasyMock.createMock(ResourceElementUpdater.class);
        EasyMock.expect(updater.remove(EasyMock.isA(Composite.class), EasyMock.isA(Contribution.class), EasyMock.isA(Set.class)))
                .andReturn(Collections.<ModelObject>emptySet());
        EasyMock.replay(updater);
        Map map = Collections.singletonMap(Composite.class.getName(), updater);
        store.setUpdaters(map);
        store.remove(contributionUri, composite);

        EasyMock.verify(updater);
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

        composite = new Composite(new QName("test", "test"));

    }

    private void createContributions() throws MalformedURLException {

        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, 1, "application/xml", false);
        URI profileUri = URI.create("profile");
        contribution.addProfile(profileUri);

        otherContributionUri = URI.create("otherContribution");
        URL otherLocationUrl = new URL("file://test");
        otherContribution = new Contribution(otherContributionUri, null, otherLocationUrl, 1, "application/xml", false);

        wireContributions();
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
