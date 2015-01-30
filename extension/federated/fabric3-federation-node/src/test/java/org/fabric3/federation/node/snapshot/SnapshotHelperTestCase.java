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
package org.fabric3.federation.node.snapshot;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.remote.RemoteImplementation;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;

/**
 *
 */
@SuppressWarnings("unchecked")
public class SnapshotHelperTestCase extends TestCase {
    private JavaServiceContract contract;
    private Service service;
    private LogicalService logicalService;
    private LogicalComponent component;
    private LogicalCompositeComponent domain;
    private LogicalChannel channel;
    private Composite composite;

    public void testSnapshotContract() throws Exception {
        RemoteServiceContract snapshot = SnapshotHelper.snapshot(contract);
        assertEquals(Bar.class.getName(), snapshot.getInterfaceName());
        assertTrue(snapshot.getSuperTypes().contains(Foo.class.getName()));
        assertEquals(BarResponse.class.getName(), snapshot.getCallbackContract().getQualifiedInterfaceName());
    }

    public void testSnapshotChannel() throws Exception {
        LogicalChannel snapshot = SnapshotHelper.snapshot(channel, composite, LogicalState.MARKED, domain);
        assertNotNull(snapshot.getDefinition());
        assertEquals(LogicalState.MARKED, snapshot.getState());
    }

    public void testSnapshotServiceDefinition() throws Exception {
        Service<ComponentType> snapshot = SnapshotHelper.snapshot(service);
        assertEquals("service", snapshot.getName());
        assertNotNull(snapshot.getServiceContract());
    }

    public void testSnapshotService() throws Exception {
        LogicalService snapshot = SnapshotHelper.snapshot(logicalService, component);
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

        service = new Service("service", contract);

        logicalService = new LogicalService(URI.create("service"), service, null);
        LogicalBinding binding = new LogicalBinding(null, logicalService);
        logicalService.addBinding(binding);

        RemoteImplementation componentImpl = new RemoteImplementation();
        ComponentType componentType = new ComponentType();
        componentType.add(service);
        componentImpl.setComponentType(componentType);
        Component<RemoteImplementation> component = new Component<>("domain", componentImpl);
        this.component = new LogicalComponent(URI.create("component"), component, null);
        component.setContributionUri(URI.create("contribution"));
        LogicalService componentService = new LogicalService(URI.create("componentService"), service, this.component);
        this.component.addService(componentService);

        composite = new Composite(new QName("test", "test"));
        // composite.add(serviceDefinition);
        CompositeImplementation domainImpl = new CompositeImplementation();
        domainImpl.setComponentType(composite);
        Component<CompositeImplementation> domainDefinition = new Component<>("domain", domainImpl);
        domain = new LogicalCompositeComponent(URI.create("domain"), domainDefinition, null);
        domain.addComponent(this.component);

        Channel channel = new Channel("Channel");
        this.channel = new LogicalChannel(URI.create("channel"), channel, null);
    }

    private interface Foo {

    }

    private interface Bar extends Foo {

    }

    private interface BarResponse {

    }

}
