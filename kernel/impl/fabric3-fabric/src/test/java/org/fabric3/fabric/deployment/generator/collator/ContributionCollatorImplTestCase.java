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
package org.fabric3.fabric.deployment.generator.collator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.deployment.generator.GenerationType;
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
        List<LogicalComponent<?>> components = new ArrayList<LogicalComponent<?>>();
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
