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
package org.fabric3.fabric.domain.generator.extension;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.container.command.AttachWireCommand;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.fabric.container.command.DetachWireCommand;
import org.fabric3.fabric.container.command.ProvisionExtensionsCommand;
import org.fabric3.fabric.container.command.UnProvisionExtensionsCommand;
import org.fabric3.fabric.domain.generator.GenerationType;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 *
 */
public class ExtensionGeneratorImplTestCase extends TestCase {

    private ExtensionGenerator generator;
    private MetaDataStore store;
    private List<LogicalComponent<?>> components;
    private Map<String, List<Contribution>> contributions;
    private Map<String, List<CompensatableCommand>> deploymentCommands;
    private URI extensionUri;
    private URI componentExtensionUri;
    private URI bindingExtensionUri;
    private URI importedExtensionUri;
    private URI interceptorExtensionUri;


    public void testIncrementalCapabilities() throws Exception {
        deploymentCommands = createDeploymentCommands(interceptorExtensionUri, true);


        Map<String, CompensatableCommand> ret = generator.generate(contributions, components, deploymentCommands, GenerationType.INCREMENTAL);
        ProvisionExtensionsCommand command = (ProvisionExtensionsCommand) ret.get("zone1");
        assertTrue(command.getExtensionUris().contains(extensionUri));
        assertTrue(command.getExtensionUris().contains(importedExtensionUri));
        assertTrue(command.getExtensionUris().contains(componentExtensionUri));
        assertTrue(command.getExtensionUris().contains(bindingExtensionUri));
        EasyMock.verify(store);
    }

    public void testUndeployCapabilities() throws Exception {
        deploymentCommands = createDeploymentCommands(interceptorExtensionUri, false);
        components.get(0).setState(LogicalState.MARKED);
        components.get(0).getReference("reference").getBindings().get(0).setState(LogicalState.MARKED);
        components.get(0).getService("service").getBindings().get(0).setState(LogicalState.MARKED);
        Map<String, CompensatableCommand> ret = generator.generate(contributions, components, deploymentCommands, GenerationType.UNDEPLOY);
        UnProvisionExtensionsCommand command = (UnProvisionExtensionsCommand) ret.get("zone1");
        assertTrue(command.getExtensionUris().contains(extensionUri));
        assertTrue(command.getExtensionUris().contains(importedExtensionUri));
        assertTrue(command.getExtensionUris().contains(componentExtensionUri));
        assertTrue(command.getExtensionUris().contains(bindingExtensionUri));
        EasyMock.verify(store);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = EasyMock.createMock(MetaDataStore.class);
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER);

        generator = new ExtensionGeneratorImpl(store, info);

        createComponent();

        extensionUri = URI.create("extensionUri");
        Set<Contribution> extensions = Collections.singleton(new Contribution(extensionUri));

        componentExtensionUri = URI.create("componentExtension");
        Set<Contribution> componentExtensions = Collections.singleton(new Contribution(componentExtensionUri));

        bindingExtensionUri = URI.create("bindingExtension");
        Set<Contribution> bindingExtensions = Collections.singleton(new Contribution(bindingExtensionUri));

        importedExtensionUri = URI.create("importedExtension");
        Contribution importedExtension = new Contribution(importedExtensionUri);

        interceptorExtensionUri = URI.create("interceptorExtension");
        Contribution interceptorExtension = new Contribution(interceptorExtensionUri);
        interceptorExtension.addWire(new MockContributionWire(importedExtensionUri));

        URI contributionUri = URI.create("app");
        List<Contribution> contributions = Collections.singletonList(new Contribution(contributionUri));
        this.contributions = Collections.singletonMap("zone1", contributions);

        EasyMock.expect(store.resolveCapabilities(EasyMock.isA(Contribution.class))).andReturn(extensions).times(3);
        EasyMock.expect(store.resolveCapability("componentCapability")).andReturn(componentExtensions);
        EasyMock.expect(store.resolveCapability("bindingCapability")).andReturn(bindingExtensions).times(2);
        EasyMock.expect(store.find(interceptorExtensionUri)).andReturn(interceptorExtension);
        EasyMock.expect(store.find(importedExtensionUri)).andReturn(importedExtension);

        EasyMock.replay(info, store);

    }

    private void createComponent() {
        ComponentType type = new ComponentType();
        type.addRequiredCapability("componentCapability");
        MockImplementation implementation = new MockImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<MockImplementation> definition = new ComponentDefinition<>("test", implementation);
        URI uri = URI.create("test");
        LogicalComponent<MockImplementation> component = new LogicalComponent<>(uri, definition, null);
        component.setZone("zone1");

        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", Multiplicity.ONE_ONE);
        LogicalReference reference = new LogicalReference(URI.create("test#reference"), referenceDefinition, component);
        MockBinding bindingDefinition = new MockBinding();
        bindingDefinition.addRequiredCapability("bindingCapability");
        LogicalBinding binding = new LogicalBinding<>(bindingDefinition, reference);
        reference.addBinding(binding);
        component.addReference(reference);

        ServiceDefinition serviceDefinition = new ServiceDefinition("service");
        LogicalService service = new LogicalService(URI.create("test#service"), serviceDefinition, component);
        service.addBinding(binding);
        component.addService(service);

        components = new ArrayList<>();
        components.add(component);
    }

    private Map<String, List<CompensatableCommand>> createDeploymentCommands(URI interceptorExtensionUri, boolean attach) {
        Map<String, List<CompensatableCommand>> deploymentCommands = new HashMap<>();
        List<CompensatableCommand> zone1Commands = new ArrayList<>();
        deploymentCommands.put("zone1", zone1Commands);
        ConnectionCommand connectionCommand = new ConnectionCommand(URI.create("component"));

        PhysicalOperationDefinition operationDefinition = new PhysicalOperationDefinition();
        PhysicalInterceptorDefinition interceptorDefinition = new PhysicalInterceptorDefinition();
        interceptorDefinition.setPolicyClassLoaderId(interceptorExtensionUri);
        operationDefinition.addInterceptor(interceptorDefinition);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(null, null, Collections.singleton(operationDefinition));
        if (attach) {
            AttachWireCommand wireCommand = new AttachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            connectionCommand.add(wireCommand);
        } else {
            DetachWireCommand wireCommand = new DetachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            connectionCommand.add(wireCommand);
        }

        zone1Commands.add(connectionCommand);
        return deploymentCommands;
    }


    private class MockBinding extends BindingDefinition {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super(null, null);
        }
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = 7965669678990461548L;

        public QName getType() {
            return null;
        }
    }


    private class MockContributionWire implements ContributionWire {
        private static final long serialVersionUID = -8513574148912964583L;
        URI exportedUri;

        private MockContributionWire(URI exportedUri) {
            this.exportedUri = exportedUri;
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
            return exportedUri;
        }

        public boolean resolves(Symbol resource) {
            return false;
        }
    }


}
