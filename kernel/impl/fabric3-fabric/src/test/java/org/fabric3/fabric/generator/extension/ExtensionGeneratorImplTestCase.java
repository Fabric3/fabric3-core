/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.fabric.generator.extension;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.ProvisionExtensionsCommand;
import org.fabric3.fabric.generator.GenerationType;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * @version $Rev$ $Date$
 */
public class ExtensionGeneratorImplTestCase extends TestCase {

    private ExtensionGenerator generator;
    private MetaDataStore store;
    private ArrayList<LogicalComponent<?>> components;


    public void testResolve() throws Exception {

        Set<Contribution> extensions = new HashSet<Contribution>();
        URI extensionUri = URI.create("extensionUri");
        extensions.add(new Contribution(extensionUri));

        Set<Contribution> componentExtensions = new HashSet<Contribution>();
        URI componentExtensionUri = URI.create("componentExtension");
        componentExtensions.add(new Contribution(componentExtensionUri));

        Set<Contribution> bindingExtensions = new HashSet<Contribution>();
        URI bindingExtensionUri = URI.create("bindingExtension");
        bindingExtensions.add(new Contribution(bindingExtensionUri));

        List<Contribution> contributions = new ArrayList<Contribution>();
        URI contributionUri = URI.create("app");
        contributions.add(new Contribution(contributionUri));
        Map<String, List<Contribution>> map = new HashMap<String, List<Contribution>>();
        map.put("zone1", contributions);

        EasyMock.expect(store.resolveCapabilities(EasyMock.isA(Contribution.class))).andReturn(extensions);
        EasyMock.expect(store.resolveCapability("componentCapability")).andReturn(componentExtensions);
        EasyMock.expect(store.resolveCapability("bindingCapability")).andReturn(bindingExtensions);

        EasyMock.replay(store);
        Map<String, List<CompensatableCommand>> deploymentCommands = new HashMap<String, List<CompensatableCommand>>();
        Map<String, CompensatableCommand> ret = generator.generate(map, components, deploymentCommands, GenerationType.INCREMENTAL);
        Command commands = ret.get("zone1");
        ProvisionExtensionsCommand command = (ProvisionExtensionsCommand) commands;
        assertTrue(command.getExtensionUris().contains(extensionUri));
        assertTrue(command.getExtensionUris().contains(componentExtensionUri));
        assertTrue(command.getExtensionUris().contains(bindingExtensionUri));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = EasyMock.createMock(MetaDataStore.class);
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER);
        EasyMock.replay(info);
        generator = new ExtensionGeneratorImpl(store, info);

        // setup components
        MockImplementation implementation = new MockImplementation();
        InjectingComponentType type = new InjectingComponentType();
        type.addRequiredCapability("componentCapability");
        implementation.setComponentType(type);
        ComponentDefinition<MockImplementation> definition = new ComponentDefinition<MockImplementation>("test", implementation);
        URI uri = URI.create("test");
        LogicalComponent<MockImplementation> component = new LogicalComponent<MockImplementation>(uri, definition, null);
        component.setZone("zone1");
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", Multiplicity.ONE_ONE);
        LogicalReference reference = new LogicalReference(URI.create("test#referemce"), referenceDefinition, component);
        MockBinding bindingDefiniton = new MockBinding();
        bindingDefiniton.addRequiredCapability("bindingCapability");
        LogicalBinding binding = new LogicalBinding<MockBinding>(bindingDefiniton, reference);
        reference.addBinding(binding);
        component.addReference(reference);
        components = new ArrayList<LogicalComponent<?>>();
        components.add(component);


    }

    private class MockBinding extends BindingDefinition {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super(null, null);
        }
    }

    private class MockImplementation extends Implementation<InjectingComponentType> {
        private static final long serialVersionUID = 7965669678990461548L;

        public QName getType() {
            return null;
        }
    }


}
