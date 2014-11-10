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
package org.fabric3.fabric.container.command;

import java.util.List;

import junit.framework.TestCase;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

public class ConnectionCommandTestCase extends TestCase {
    private ConnectionCommand command;
    private AttachWireCommand attachCommand1;
    private AttachWireCommand attachCommand2;
    private AttachWireCommand attachCommand3;
    private DetachWireCommand detachCommand1;
    private DetachWireCommand detachCommand2;
    private DetachWireCommand detachCommand3;

    public void testCompensatingCommand() throws Exception {
        ConnectionCommand compensating = command.getCompensatingCommand();

        List<AttachWireCommand> attachCommands = compensating.getAttachCommands();
        assertEquals(3, attachCommands.size());
        assertEquals(detachCommand3.getPhysicalWireDefinition(), attachCommands.get(0).getPhysicalWireDefinition());
        assertEquals(detachCommand2.getPhysicalWireDefinition(), attachCommands.get(1).getPhysicalWireDefinition());
        assertEquals(detachCommand1.getPhysicalWireDefinition(), attachCommands.get(2).getPhysicalWireDefinition());

        List<DetachWireCommand> detachCommands = compensating.getDetachCommands();
        assertEquals(3, detachCommands.size());
        assertEquals(attachCommand3.getPhysicalWireDefinition(), detachCommands.get(0).getPhysicalWireDefinition());
        assertEquals(attachCommand2.getPhysicalWireDefinition(), detachCommands.get(1).getPhysicalWireDefinition());
        assertEquals(attachCommand1.getPhysicalWireDefinition(), detachCommands.get(2).getPhysicalWireDefinition());
    }

    public void testNoAttachDetachCompensatingCommand() throws Exception {
        ConnectionCommand emptyCommand = new ConnectionCommand();
        ConnectionCommand compensating = emptyCommand.getCompensatingCommand();
        assertTrue(compensating.getAttachCommands().isEmpty());
        assertTrue(compensating.getDetachCommands().isEmpty());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new ConnectionCommand();
        attachCommand1 = new AttachWireCommand();
        attachCommand1.setPhysicalWireDefinition(new MockDefinition());
        command.add(attachCommand1);

        attachCommand2 = new AttachWireCommand();
        attachCommand2.setPhysicalWireDefinition(new MockDefinition());
        command.add(attachCommand2);

        attachCommand3 = new AttachWireCommand();
        attachCommand3.setPhysicalWireDefinition(new MockDefinition());
        command.add(attachCommand3);

        detachCommand1 = new DetachWireCommand();
        detachCommand1.setPhysicalWireDefinition(new MockDefinition());
        command.add(detachCommand1);

        detachCommand2 = new DetachWireCommand();
        detachCommand2.setPhysicalWireDefinition(new MockDefinition());
        command.add(detachCommand2);

        detachCommand3 = new DetachWireCommand();
        detachCommand3.setPhysicalWireDefinition(new MockDefinition());
        command.add(detachCommand3);
    }

    private class MockDefinition extends PhysicalWireDefinition {
        private static final long serialVersionUID = 995196092611674935L;

        public MockDefinition() {
            super(null, null, null);
        }
    }
}
