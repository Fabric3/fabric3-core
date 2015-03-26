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
package org.fabric3.fabric.domain.generator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWire;

/**
 *
 */
public class ResourceReferenceCommandGeneratorTestCase extends TestCase {
    private WireGenerator wireGenerator;

    @SuppressWarnings({"unchecked"})
    public void testGenerate() throws Exception {
        MockWire wireDefinition = new MockWire();
        EasyMock.expect(wireGenerator.generateResource(EasyMock.isA(LogicalResourceReference.class))).andReturn(wireDefinition);
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();

        ResourceReferenceCommandGenerator generator = new ResourceReferenceCommandGenerator(wireGenerator);
        ConnectionCommand command = generator.generate(component).get();
        assertEquals(1, command.getAttachCommands().size());

        EasyMock.verify(wireGenerator);
    }

    public void testNoGenerate() throws Exception {
        EasyMock.replay(wireGenerator);
        LogicalComponent<?> component = createComponent();
        component.setState(LogicalState.PROVISIONED);

        ResourceReferenceCommandGenerator generator = new ResourceReferenceCommandGenerator(wireGenerator);
        assertFalse(generator.generate(component).isPresent());

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
        public String getQualifiedInterfaceName() {
            return "test";
        }
    }

    private class MockWire extends PhysicalWire {
        private MockWire() {
            super(null, null, null);
        }
    }

}