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
package org.fabric3.fabric.deployment.command;

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
