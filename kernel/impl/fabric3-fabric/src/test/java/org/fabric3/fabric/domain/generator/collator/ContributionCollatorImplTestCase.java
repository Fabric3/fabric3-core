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
package org.fabric3.fabric.domain.generator.collator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.domain.generator.GenerationType;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class ContributionCollatorImplTestCase extends TestCase {
    private static final URI CONTRIBUTION1 = URI.create("contribution1");
    private static final URI CONTRIBUTION2 = URI.create("contribution2");
    private static final URI CONTRIBUTION3 = URI.create("contribution3");
    private static final URI COMPONENT2 = URI.create("component2");

    private Contribution contribution1;
    private Contribution contribution2;
    private Contribution contribution3;


    public void testIncrementalCollateContributions() throws Exception {
        MetaDataStore store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.find(CONTRIBUTION1)).andReturn(contribution1);
        EasyMock.expect(store.find(CONTRIBUTION2)).andReturn(contribution2);
        EasyMock.expect(store.find(CONTRIBUTION3)).andReturn(contribution3);
        EasyMock.replay(store);

        ContributionCollatorImpl collator = new ContributionCollatorImpl(store);
        List<LogicalComponent<?>> components = createComponents();
        Map<String, List<Contribution>> collated = collator.collateContributions(components, GenerationType.INCREMENTAL);
        List<Contribution> zone1 = collated.get("zone1");
        assertEquals(2, zone1.size());
        assertTrue(zone1.contains(contribution1));
        assertTrue(zone1.contains(contribution2));
        List<Contribution> zone2 = collated.get("zone2");
        assertEquals(1, zone2.size());
        assertTrue(zone2.contains(contribution3));
        EasyMock.verify(store);
    }

    public void testIncrementalAlreadyDeployedCollateContributions() throws Exception {
        MetaDataStore store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.find(CONTRIBUTION1)).andReturn(contribution1);
        EasyMock.expect(store.find(CONTRIBUTION2)).andReturn(contribution2);
        EasyMock.replay(store);

        ContributionCollatorImpl collator = new ContributionCollatorImpl(store);
        List<LogicalComponent<?>> components = createComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getUri().equals(COMPONENT2)){
                // the third contribution associated with component1 should not be included
                component.setState(LogicalState.PROVISIONED);
                break;
            }
        }
        Map<String, List<Contribution>> collated = collator.collateContributions(components, GenerationType.INCREMENTAL);
        List<Contribution> zone1 = collated.get("zone1");
        assertEquals(2, zone1.size());
        assertTrue(zone1.contains(contribution1));
        assertTrue(zone1.contains(contribution2));
        assertNull(collated.get("zone2"));
        EasyMock.verify(store);
    }

    public void testFullAlreadyDeployedCollateContributions() throws Exception {
        MetaDataStore store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.find(CONTRIBUTION1)).andReturn(contribution1);
        EasyMock.expect(store.find(CONTRIBUTION2)).andReturn(contribution2);
        EasyMock.expect(store.find(CONTRIBUTION3)).andReturn(contribution3);
        EasyMock.replay(store);

        ContributionCollatorImpl collator = new ContributionCollatorImpl(store);
        List<LogicalComponent<?>> components = createComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getUri().equals(COMPONENT2)){
                // the third contribution associated with component1 should still be included
                component.setState(LogicalState.PROVISIONED);
                break;
            }
        }
        Map<String, List<Contribution>> collated = collator.collateContributions(components, GenerationType.FULL);
        List<Contribution> zone1 = collated.get("zone1");
        assertEquals(2, zone1.size());
        assertTrue(zone1.contains(contribution1));
        assertTrue(zone1.contains(contribution2));
        List<Contribution> zone2 = collated.get("zone2");
        assertEquals(1, zone2.size());
        assertTrue(zone2.contains(contribution3));
        EasyMock.verify(store);
    }

    public void testUndeployCollateContributions() throws Exception {
        MetaDataStore store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.find(CONTRIBUTION3)).andReturn(contribution3);
        EasyMock.replay(store);

        ContributionCollatorImpl collator = new ContributionCollatorImpl(store);
        List<LogicalComponent<?>> components = createComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getUri().equals(COMPONENT2)){
                // the third contribution associated with component1 should still be included
                component.setState(LogicalState.MARKED);
                break;
            }
        }
        Map<String, List<Contribution>> collated = collator.collateContributions(components, GenerationType.UNDEPLOY);
        assertNull(collated.get("zone1"));
        List<Contribution> zone2 = collated.get("zone2");
        assertEquals(1, zone2.size());
        assertTrue(zone2.contains(contribution3));
        EasyMock.verify(store);
    }

    @SuppressWarnings({"unchecked"})
    private List<LogicalComponent<?>> createComponents() {
        List<LogicalComponent<?>> components = new ArrayList<>();
        ComponentDefinition definition1 = new ComponentDefinition("component1", null);
        definition1.setContributionUri(CONTRIBUTION1);
        LogicalComponent<?>component1 = new LogicalComponent(URI.create("component1"), definition1, null);
        component1.setZone("zone1");
        components.add(component1);
        ComponentDefinition definition2 = new ComponentDefinition("component2", null);
        definition2.setContributionUri(CONTRIBUTION3);
        LogicalComponent<?> component2 = new LogicalComponent(COMPONENT2, definition2, null);
        component2.setZone("zone2");
        components.add(component2);
        return components;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // setup a contribution for each component and one contribution imported by the first contribution
        contribution1 = new Contribution(CONTRIBUTION1);
        contribution1.addWire(new MockContributionWire(CONTRIBUTION2));
        contribution2 = new Contribution(CONTRIBUTION2);
        contribution3 = new Contribution(CONTRIBUTION3);

    }

    private class MockContributionWire implements ContributionWire {
        private static final long serialVersionUID = -8513574148912964583L;
        private URI exported;

        private MockContributionWire(URI exported) {
            this.exported = exported;
        }

        public Import getImport() {
            return null;
        }

        public Export getExport() {
            return null;
        }

        public URI getImportContributionUri() {
            return null;
        }

        public URI getExportContributionUri() {
            return exported;
        }

        public boolean resolves(Symbol resource) {
            return false;
        }
    }
}
