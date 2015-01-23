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
package org.fabric3.fabric.container.component.scope;

import javax.xml.namespace.QName;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.federation.topology.NodeTopologyService;

/**
 *
 */
public class DomainScopeContainerTestCase extends TestCase {
    private DomainScopeContainer scopeContainer;
    private ScopedComponent component;
    private Object instance;
    private QName deployable;
    private HostInfo info;

    public void testSingleVMStart() throws Exception {
        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component, info);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stopContext(deployable);
        EasyMock.verify(component, info);
    }

    public void testZoneLeaderStart() throws Exception {
        NodeTopologyService topologyService = EasyMock.createMock(NodeTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stopContext(deployable);
        EasyMock.verify(component, info, topologyService);
    }

    public void testNotZoneLeaderNoStart() throws Exception {
        NodeTopologyService topologyService = EasyMock.createMock(NodeTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stopContext(deployable);
        EasyMock.verify(component, info, topologyService);
    }

    public void testZoneLeaderElectedStart() throws Exception {
        NodeTopologyService topologyService = EasyMock.createMock(NodeTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);

        scopeContainer.onLeaderElected("runtime");

        scopeContainer.stopContext(deployable);
        EasyMock.verify(component, info, topologyService);
    }

    public void testStopContainer() throws Exception {
        NodeTopologyService topologyService = EasyMock.createMock(NodeTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false).times(2);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stop();

        scopeContainer.start();
        scopeContainer.startContext(deployable);

        EasyMock.verify(component, info, topologyService);
    }

    public void testStopAllContexts() throws Exception {
        NodeTopologyService topologyService = EasyMock.createMock(NodeTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stopAllContexts();

        EasyMock.verify(component, info, topologyService);
    }

    public void testStopContext() throws Exception {
        NodeTopologyService topologyService = EasyMock.createMock(NodeTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stopContext(deployable);

        EasyMock.verify(component, info, topologyService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deployable = new QName("deployable");

        info = EasyMock.createMock(HostInfo.class);
        component = EasyMock.createMock(ScopedComponent.class);
        EasyMock.expect(component.getDeployable()).andReturn(deployable).anyTimes();

        instance = new Object();

        ScopeContainerMonitor monitor = EasyMock.createNiceMock(ScopeContainerMonitor.class);
        EasyMock.replay(monitor);
        scopeContainer = new DomainScopeContainer(info, monitor);
        scopeContainer.start();

    }
}
