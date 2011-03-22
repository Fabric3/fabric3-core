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
*/
package org.fabric3.monitor.runtime;


import java.net.URI;
import java.util.Collections;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class LogBackMonitorServiceTestCase extends TestCase {
    private ComponentManager componentManager;
    private HostInfo info;
    private Component component;

    public void testSetDeployableLevelAtStartup() throws Exception {
        QName deployable = new QName("foo", "bar");
        EasyMock.expect(componentManager.getDeployedComponents(deployable)).andReturn(Collections.<Component>singletonList(component));
        component.setLevel(MonitorLevel.DEBUG);
        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setDeployableLevels(Collections.<QName, String>singletonMap(deployable, "debug"));
        service.init();
        EasyMock.verify(componentManager, info, component);
    }

    public void testSetDeployableLevelNotFoundAtStartup() throws Exception {
        QName deployable = new QName("foo", "bar");
        // return no deployable to simulate setting a deployable that is not yet deployed
        EasyMock.expect(componentManager.getDeployedComponents(deployable)).andReturn(Collections.<Component>emptyList());
        EasyMock.replay(componentManager, info);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setDeployableLevels(Collections.<QName, String>singletonMap(deployable, "debug"));
        service.init();
        EasyMock.verify(componentManager, info);
    }

    public void testSetDeployableLevelAfterStartup() throws Exception {
        QName deployable = new QName("foo", "bar");
        URI uri = URI.create("fabric3://Component");
        EasyMock.expect(componentManager.getDeployedComponents(deployable)).andReturn(Collections.<Component>emptyList());
        EasyMock.expect(component.getUri()).andReturn(uri).atLeastOnce();
        EasyMock.expect(component.getDeployable()).andReturn(deployable).atLeastOnce();
        component.setLevel(MonitorLevel.DEBUG);
        EasyMock.replay(componentManager, info, component);

        PhysicalComponentDefinition definition = new MockDefinition();

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setDeployableLevels(Collections.<QName, String>singletonMap(deployable, "debug"));
        service.init();

        service.onBuild(component, definition);

        EasyMock.verify(componentManager, info, component);
    }

    public void testSetRuntimeUriLevelAtStartup() throws Exception {
        URI uri = URI.create(Names.RUNTIME_NAME + "/Component");
        EasyMock.expect(componentManager.getComponentsInHierarchy(uri)).andReturn(Collections.<Component>singletonList(component));
        component.setLevel(MonitorLevel.DEBUG);
        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setRuntimeComponentLevels(Collections.<String, String>singletonMap("Component", "debug"));
        service.init();
        EasyMock.verify(componentManager, info, component);
    }

    public void testSetRuntimeUriLevelNotFoundAtStartup() throws Exception {
        EasyMock.expect(componentManager.getComponentsInHierarchy(EasyMock.isA(URI.class))).andReturn(Collections.<Component>emptyList());
        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setRuntimeComponentLevels(Collections.<String, String>singletonMap("NotFoundComponent", "debug"));
        service.init();
        EasyMock.verify(componentManager, info, component);
    }

    public void testSetRuntimeUriLevelAfterStartup() throws Exception {
        URI uri = URI.create(Names.RUNTIME_NAME + "/Component");
        // return no components to simulate setting a deployable that is not yet deployed
        EasyMock.expect(componentManager.getComponentsInHierarchy(uri)).andReturn(Collections.<Component>emptyList());
        EasyMock.expect(component.getUri()).andReturn(uri).atLeastOnce();
        component.setLevel(MonitorLevel.DEBUG);
        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setRuntimeComponentLevels(Collections.<String, String>singletonMap("Component", "debug"));
        service.init();

        PhysicalComponentDefinition definition = new MockDefinition();
        service.onBuild(component, definition);

        EasyMock.verify(componentManager, info, component);
    }


    public void testSetApplicationUriLevelAtStartup() throws Exception {
        URI uri = URI.create("fabric3://domain/Component");
        EasyMock.expect(componentManager.getComponentsInHierarchy(uri)).andReturn(Collections.<Component>singletonList(component));
        component.setLevel(MonitorLevel.DEBUG);

        URI domain = URI.create("fabric3://domain");
        EasyMock.expect(info.getDomain()).andReturn(domain);

        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setApplicationComponentLevels(Collections.<String, String>singletonMap("Component", "debug"));
        service.init();
        EasyMock.verify(componentManager, info, component);
    }

    public void testSetApplicationUriLevelNotFoundAtStartup() throws Exception {
        EasyMock.expect(componentManager.getComponentsInHierarchy(EasyMock.isA(URI.class))).andReturn(Collections.<Component>emptyList());
        URI domain = URI.create("fabric3://domain");
        EasyMock.expect(info.getDomain()).andReturn(domain);
        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setApplicationComponentLevels(Collections.<String, String>singletonMap("NotFoundComponent", "debug"));
        service.init();
        EasyMock.verify(componentManager, info, component);
    }

    public void testSetApplicationUriLevelAfterStartup() throws Exception {
        URI uri = URI.create("fabric3://domain/Component");

        // return no components to simulate setting a deployable that is not yet deployed
        EasyMock.expect(componentManager.getComponentsInHierarchy(uri)).andReturn(Collections.<Component>emptyList());
        EasyMock.expect(component.getUri()).andReturn(uri).atLeastOnce();
        component.setLevel(MonitorLevel.DEBUG);

        URI domain = URI.create("fabric3://domain");
        EasyMock.expect(info.getDomain()).andReturn(domain);

        EasyMock.replay(componentManager, info, component);

        LogBackMonitorService service = new LogBackMonitorService(componentManager, info);
        service.setApplicationComponentLevels(Collections.<String, String>singletonMap("Component", "debug"));
        service.init();

        PhysicalComponentDefinition definition = new MockDefinition();
        service.onBuild(component, definition);

        EasyMock.verify(componentManager, info, component);
    }



    @Override
    protected void setUp() throws Exception {
        super.setUp();
        componentManager = EasyMock.createMock(ComponentManager.class);
        info = EasyMock.createMock(HostInfo.class);
        component = EasyMock.createMock(Component.class);
    }

    private class MockDefinition extends PhysicalComponentDefinition {
        private static final long serialVersionUID = -1266410990667874184L;
    }
}