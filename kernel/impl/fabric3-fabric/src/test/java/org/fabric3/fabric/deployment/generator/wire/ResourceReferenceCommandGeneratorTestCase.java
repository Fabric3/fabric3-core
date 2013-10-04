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
package org.fabric3.fabric.deployment.generator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.deployment.command.ConnectionCommand;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 *
 */
public class ResourceReferenceCommandGeneratorTestCase extends TestCase {
    private WireGenerator wireGenerator;

    @SuppressWarnings({"unchecked"})
    public void testGenerateIncremental() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateResource(EasyMock.isA(LogicalResourceReference.class))).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();

        ResourceReferenceCommandGenerator generator = new ResourceReferenceCommandGenerator(wireGenerator, 0);
        ConnectionCommand command = generator.generate(component, true);
        assertEquals(1, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testNoGenerateIncremental() throws Exception {
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);

        ResourceReferenceCommandGenerator generator = new ResourceReferenceCommandGenerator(wireGenerator, 0);
        assertNull(generator.generate(component, true));

        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateFull() throws Exception {
        MockWireDefinition wireDefinition = new MockWireDefinition();
        EasyMock.expect(wireGenerator.generateResource(EasyMock.isA(LogicalResourceReference.class))).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);

        ResourceReferenceCommandGenerator generator = new ResourceReferenceCommandGenerator(wireGenerator, 0);
        ConnectionCommand command = generator.generate(component, false);
        assertEquals(1, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }


    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> createComponent() {
        LogicalComponent component = new LogicalComponent(URI.create("component"), null, null);
        LogicalResourceReference resource = new LogicalResourceReference(URI.create("component#resource"), null, null);
        ServiceContract contract = new MockContract();
        resource.setServiceContract(contract);
        component.addResource(resource);
        return component;
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(WireGenerator.class);
    }

    private class MockContract extends ServiceContract {
        private static final long serialVersionUID = -2909070237720164262L;

        @Override
        public String getQualifiedInterfaceName() {
            return "test";
        }
    }

    private class MockWireDefinition extends PhysicalWireDefinition {
        private static final long serialVersionUID = 358078346745301821L;

        private MockWireDefinition() {
            super(null, null, null);
        }
    }

}