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
package org.fabric3.fabric.container.component;

import java.net.URI;
import java.util.function.Consumer;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.discovery.DiscoveryAgent;

/**
 *
 */
public class DomainScopeContainerTestCase extends TestCase {
    private DomainScopeContainer scopeContainer;
    private ScopedComponent component;
    private Object instance;
    private URI contributionUri;
    private HostInfo info;

    public void testSingleVMStart() throws Exception {
        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component, info);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);
        scopeContainer.stopContext(contributionUri);
        EasyMock.verify(component, info);
    }

    public void testZoneLeaderStart() throws Exception {
        DiscoveryAgent discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(true);

        scopeContainer.discoveryAgent = discoveryAgent;

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component, info, discoveryAgent);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);
        scopeContainer.stopContext(contributionUri);
        EasyMock.verify(component, info, discoveryAgent);
    }

    public void testNotZoneLeaderNoStart() throws Exception {
        DiscoveryAgent discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(false);

        scopeContainer.discoveryAgent = discoveryAgent;

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, discoveryAgent);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);
        scopeContainer.stopContext(contributionUri);
        EasyMock.verify(component, info, discoveryAgent);
    }

    public void testZoneLeaderElectedStart() throws Exception {
        DiscoveryAgent discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(false);

        scopeContainer.discoveryAgent = discoveryAgent;

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component, info, discoveryAgent);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);

        scopeContainer.onLeaderChange(true);

        scopeContainer.stopContext(contributionUri);
        EasyMock.verify(component, info, discoveryAgent);
    }

    @SuppressWarnings("unchecked")
    public void testStopContainer() throws Exception {
        DiscoveryAgent discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(false).times(2);

        scopeContainer.discoveryAgent = discoveryAgent;

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, discoveryAgent);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);
        scopeContainer.stop();

        scopeContainer.start();
        scopeContainer.startContext(contributionUri);

        EasyMock.verify(component, info, discoveryAgent);
    }

    public void testStopAllContexts() throws Exception {
        DiscoveryAgent discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);
        EasyMock.expect(discoveryAgent.isLeader()).andReturn(true);

        scopeContainer.discoveryAgent = discoveryAgent;

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, discoveryAgent);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);
        scopeContainer.stopAllContexts();

        EasyMock.verify(component, info, discoveryAgent);
    }

    public void testStopContext() throws Exception {
        DiscoveryAgent topologyService = EasyMock.createMock(DiscoveryAgent.class);
        EasyMock.expect(topologyService.isLeader()).andReturn(true);

        scopeContainer.discoveryAgent = topologyService;

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();

        EasyMock.replay(component, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(contributionUri);
        scopeContainer.stopContext(contributionUri);

        EasyMock.verify(component, info, topologyService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        contributionUri = URI.create("test");

        info = EasyMock.createMock(HostInfo.class);
        component = EasyMock.createMock(ScopedComponent.class);
        EasyMock.expect(component.getContributionUri()).andReturn(contributionUri).anyTimes();

        instance = new Object();

        ScopeContainerMonitor monitor = EasyMock.createNiceMock(ScopeContainerMonitor.class);
        EasyMock.replay(monitor);
        scopeContainer = new DomainScopeContainer(info, monitor);
        scopeContainer.start();

    }
}
