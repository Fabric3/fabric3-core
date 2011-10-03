/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.component.scope;

import java.util.Collections;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $$Rev$$ $$Date$$
 */
public class DomainScopeContainerTestCase extends TestCase {
    private DomainScopeContainer scopeContainer;
    private AtomicComponent component;
    private InstanceWrapper wrapper;
    private WorkContext workContext;
    private QName deployable;
    private HostInfo info;

    public void testSingleVMStart() throws Exception {
        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstanceWrapper(EasyMock.isA(WorkContext.class))).andReturn(wrapper);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();

        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        wrapper.stop(EasyMock.isA(WorkContext.class));

        EasyMock.replay(component, wrapper, info);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);
        scopeContainer.stopContext(deployable, workContext);
        EasyMock.verify(component, wrapper, info);
    }

    public void testZoneLeaderStart() throws Exception {
        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstanceWrapper(EasyMock.isA(WorkContext.class))).andReturn(wrapper);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT).atLeastOnce();

        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        wrapper.stop(EasyMock.isA(WorkContext.class));

        EasyMock.replay(component, wrapper, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);
        scopeContainer.stopContext(deployable, workContext);
        EasyMock.verify(component, wrapper, info, topologyService);
    }

    public void testNotZoneLeaderNoStart() throws Exception {
        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT).atLeastOnce();

        EasyMock.replay(component, wrapper, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);
        scopeContainer.stopContext(deployable, workContext);
        EasyMock.verify(component, wrapper, info, topologyService);
    }

    public void testZoneLeaderElectedStart() throws Exception {
        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstanceWrapper(EasyMock.isA(WorkContext.class))).andReturn(wrapper);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT).atLeastOnce();

        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        wrapper.stop(EasyMock.isA(WorkContext.class));

        EasyMock.replay(component, wrapper, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);

        scopeContainer.onLeaderElected("runtime");

        scopeContainer.stopContext(deployable, workContext);
        EasyMock.verify(component, wrapper, info, topologyService);
    }

    public void testStopContainer() throws Exception {
        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(false).times(2);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT).atLeastOnce();

        EasyMock.replay(component, wrapper, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);
        scopeContainer.stop();

        scopeContainer.start();
        scopeContainer.startContext(deployable, workContext);

        EasyMock.verify(component, wrapper, info, topologyService);
    }

    public void testStopAllContexts() throws Exception {
        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstanceWrapper(EasyMock.isA(WorkContext.class))).andReturn(wrapper);

        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        wrapper.stop(EasyMock.isA(WorkContext.class));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT).atLeastOnce();

        EasyMock.replay(component, wrapper, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);
        scopeContainer.stopAllContexts(workContext);

        EasyMock.verify(component, wrapper, info, topologyService);
    }

    public void testStopContext() throws Exception {
        ZoneTopologyService topologyService = EasyMock.createMock(ZoneTopologyService.class);
        EasyMock.expect(topologyService.isZoneLeader()).andReturn(true);

        scopeContainer.setTopologyService(Collections.singletonList(topologyService));

        EasyMock.expect(component.isEagerInit()).andReturn(true);
        EasyMock.expect(component.createInstanceWrapper(EasyMock.isA(WorkContext.class))).andReturn(wrapper);

        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        wrapper.stop(EasyMock.isA(WorkContext.class));

        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.PARTICIPANT).atLeastOnce();

        EasyMock.replay(component, wrapper, info, topologyService);

        scopeContainer.register(component);
        scopeContainer.startContext(deployable, workContext);
        scopeContainer.stopContext(deployable, workContext);

        EasyMock.verify(component, wrapper, info, topologyService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workContext = new WorkContext();
        deployable = new QName("deployable");

        info = EasyMock.createMock(HostInfo.class);
        component = EasyMock.createMock(AtomicComponent.class);
        EasyMock.expect(component.getDeployable()).andReturn(deployable).anyTimes();

        wrapper = EasyMock.createMock(InstanceWrapper.class);

        ScopeContainerMonitor monitor = EasyMock.createNiceMock(ScopeContainerMonitor.class);
        EasyMock.replay(monitor);
        scopeContainer = new DomainScopeContainer(info, monitor);
        scopeContainer.start();

    }
}
