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
package org.fabric3.federation.node.snapshot;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.model.type.component.AbstractService;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.remote.RemoteImplementation;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;

/**
 *
 */
@SuppressWarnings("unchecked")
public class SnapshotHelperTestCase extends TestCase {
    private JavaServiceContract contract;
    private ServiceDefinition serviceDefinition;
    private LogicalService service;
    private LogicalComponent component;
    private LogicalCompositeComponent domain;

    public void testSnapshotContract() throws Exception {
        RemoteServiceContract snapshot = SnapshotHelper.snapshot(contract);
        assertEquals(Bar.class.getName(), snapshot.getInterfaceName());
        assertTrue(snapshot.getSuperTypes().contains(Foo.class.getName()));
        assertEquals(BarResponse.class.getName(), snapshot.getCallbackContract().getQualifiedInterfaceName());
    }

    public void testSnapshotServiceDefinition() throws Exception {
        AbstractService snapshot = SnapshotHelper.snapshot(serviceDefinition);
        assertEquals("service", snapshot.getName());
        assertNotNull(snapshot.getServiceContract());
    }

    public void testSnapshotService() throws Exception {
        LogicalService snapshot = SnapshotHelper.snapshot(service, component);
        assertEquals("service", snapshot.getUri().toString());
        assertNotNull(snapshot.getServiceContract());
        assertNotNull(snapshot.getDefinition());
        assertFalse(snapshot.getBindings().isEmpty());
    }

    public void testSnapshotComponent() throws Exception {
        LogicalComponent snapshot = SnapshotHelper.snapshot(component, LogicalState.NEW, domain);
        assertFalse(snapshot.getServices().isEmpty());
        assertFalse(snapshot.getDefinition().getComponentType().getServices().isEmpty());
        assertEquals(LogicalState.NEW, snapshot.getState());
    }

    public void testSnapshotComposite() throws Exception {
        LogicalCompositeComponent snapshot = SnapshotHelper.snapshot(domain, null, LogicalState.NEW);
        assertFalse(snapshot.getComponents().isEmpty());
        for (LogicalComponent<?> child : snapshot.getComponents()) {
            assertEquals(LogicalState.NEW, child.getState());
        }
    }

    public void testSnapshotOnlyRemoteServices() throws Exception {
        for (LogicalComponent<?> component : domain.getComponents()) {
            for (LogicalService service : component.getServices()) {
                service.getServiceContract().setRemotable(false);
            }
        }
        LogicalCompositeComponent snapshot = SnapshotHelper.snapshot(domain, null, LogicalState.NEW);
        assertTrue(snapshot.getComponents().isEmpty());
    }

    public void testSnapshotOnlyComponentsInContribution() throws Exception {
        LogicalCompositeComponent snapshot = SnapshotHelper.snapshot(domain, URI.create("otherContribution"), LogicalState.NEW);
        assertTrue(snapshot.getComponents().isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();
        contract = new JavaServiceContract(Bar.class);
        JavaServiceContract callbackContract = new JavaServiceContract(BarResponse.class);
        contract.setCallbackContract(callbackContract);
        contract.setRemotable(true);

        serviceDefinition = new ServiceDefinition("service", contract);

        service = new LogicalService(URI.create("service"), serviceDefinition, null);
        LogicalBinding binding = new LogicalBinding(null, service);
        service.addBinding(binding);

        RemoteImplementation componentImpl = new RemoteImplementation();
        ComponentType componentType = new ComponentType();
        componentType.add(serviceDefinition);
        componentImpl.setComponentType(componentType);
        ComponentDefinition<RemoteImplementation> componentDefinition = new ComponentDefinition<RemoteImplementation>("domain", componentImpl);
        component = new LogicalComponent(URI.create("component"), componentDefinition, null);
        componentDefinition.setContributionUri(URI.create("contribution"));
        LogicalService componentService = new LogicalService(URI.create("componentService"), serviceDefinition, component);
        component.addService(componentService);

        Composite composite = new Composite(new QName("test", "test"));
        // composite.add(serviceDefinition);
        CompositeImplementation domainImpl = new CompositeImplementation();
        domainImpl.setComponentType(composite);
        ComponentDefinition<CompositeImplementation> domainDefinition = new ComponentDefinition<CompositeImplementation>("domain", domainImpl);
        domain = new LogicalCompositeComponent(URI.create("domain"), domainDefinition, null);
        domain.addComponent(component);
    }

    private interface Foo {

    }

    private interface Bar extends Foo {

    }

    private interface BarResponse {

    }

}
