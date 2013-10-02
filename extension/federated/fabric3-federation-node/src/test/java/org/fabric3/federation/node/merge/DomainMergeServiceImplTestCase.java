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
package org.fabric3.federation.node.merge;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.remote.RemoteImplementation;

/**
 *
 */
public class DomainMergeServiceImplTestCase extends TestCase {
    private DomainMergeServiceImpl mergeService;
    private LogicalComponentManager lcm;
    private LogicalCompositeComponent domain;
    private LogicalCompositeComponent snapshot;

    public void testMerge() throws Exception {
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.replay(lcm);

        mergeService.merge(snapshot);

        EasyMock.verify(lcm);

        assertFalse(domain.getComponents().isEmpty());
        for (LogicalComponent<?> component : domain.getComponents()) {
            assertEquals(LogicalState.PROVISIONED, component.getState());
        }

    }

    public void testMergeUnProvision() throws Exception {

        EasyMock.expect(lcm.getRootComponent()).andReturn(domain).atLeastOnce();
        EasyMock.replay(lcm);

        mergeService.merge(snapshot);

        // change the snapshot to un-provisioned and re-merge
        for (LogicalComponent<?> component : snapshot.getComponents()) {
            component.setState(LogicalState.MARKED);
        }

        mergeService.merge(snapshot);

        EasyMock.verify(lcm);

        assertTrue(domain.getComponents().isEmpty());

    }

    public void testDoNoMergeFromSameZone() throws Exception {
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.replay(lcm);

        for (LogicalComponent<?> component : snapshot.getComponents()) {
            component.setZone("zone0");
        }
        mergeService.merge(snapshot);

        EasyMock.verify(lcm);

        assertTrue(domain.getComponents().isEmpty());

    }

    public void testDrop() throws Exception {
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);

        LogicalComponent child2 = new LogicalComponent<RemoteImplementation>(URI.create("child2"), null, domain);
        child2.setZone("zone1");
        domain.addComponent(child2);

        EasyMock.replay(lcm);

        mergeService.drop("zone1");

        EasyMock.verify(lcm);
        assertTrue(domain.getComponents().isEmpty());

    }

    public void setUp() throws Exception {
        super.setUp();
        createDomain();
        createSnapshot();

        lcm = EasyMock.createMock(LogicalComponentManager.class);

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getZoneName()).andReturn("zone0");
        EasyMock.replay(info);

        mergeService = new DomainMergeServiceImpl(lcm, info);
    }

    private void createDomain() {
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("domain");
        definition.setImplementation(impl);
        domain = new LogicalCompositeComponent(URI.create("domain"), definition, null);
        domain.setState(LogicalState.PROVISIONED);
    }

    private void createSnapshot() {
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("domain");
        definition.setImplementation(impl);
        snapshot = new LogicalCompositeComponent(URI.create("domain"), definition, null);
        snapshot.setState(LogicalState.NEW);

        LogicalComponent<RemoteImplementation> child = new LogicalComponent<RemoteImplementation>(URI.create("remote"), null, snapshot);
        child.setState(LogicalState.NEW);
        snapshot.addComponent(child);

    }
}
