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
 */
package org.fabric3.federation.node.merge;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.component.CompositeImplementation;
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
        Component<CompositeImplementation> definition = new Component<>("domain");
        definition.setImplementation(impl);
        domain = new LogicalCompositeComponent(URI.create("domain"), definition, null);
        domain.setState(LogicalState.PROVISIONED);
    }

    private void createSnapshot() {
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        Component<CompositeImplementation> definition = new Component<>("domain");
        definition.setImplementation(impl);
        snapshot = new LogicalCompositeComponent(URI.create("domain"), definition, null);
        snapshot.setState(LogicalState.NEW);

        LogicalComponent<RemoteImplementation> child = new LogicalComponent<>(URI.create("remote"), null, snapshot);
        child.setState(LogicalState.NEW);
        snapshot.addComponent(child);

    }
}
