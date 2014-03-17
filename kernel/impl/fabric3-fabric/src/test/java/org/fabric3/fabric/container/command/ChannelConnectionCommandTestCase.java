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
package org.fabric3.fabric.container.command;

import java.util.List;

import junit.framework.TestCase;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

public class ChannelConnectionCommandTestCase extends TestCase {
    private ChannelConnectionCommand command;
    private AttachChannelConnectionCommand attachCommand1;
    private AttachChannelConnectionCommand attachCommand2;
    private AttachChannelConnectionCommand attachCommand3;
    private DetachChannelConnectionCommand detachCommand1;
    private DetachChannelConnectionCommand detachCommand2;
    private DetachChannelConnectionCommand detachCommand3;

    public void testCompensatingCommand() throws Exception {
        ChannelConnectionCommand compensating = command.getCompensatingCommand();

        // the last 
        List<AttachChannelConnectionCommand> attachCommands = compensating.getAttachCommands();
        assertEquals(3, attachCommands.size());
        assertEquals(detachCommand3.getDefinition(), attachCommands.get(0).getDefinition());
        assertEquals(detachCommand2.getDefinition(), attachCommands.get(1).getDefinition());
        assertEquals(detachCommand1.getDefinition(), attachCommands.get(2).getDefinition());

        List<DetachChannelConnectionCommand> detachCommands = compensating.getDetachCommands();
        assertEquals(3, detachCommands.size());
        assertEquals(attachCommand3.getDefinition(), detachCommands.get(0).getDefinition());
        assertEquals(attachCommand2.getDefinition(), detachCommands.get(1).getDefinition());
        assertEquals(attachCommand1.getDefinition(), detachCommands.get(2).getDefinition());
    }

    public void testNoAttachDetachCompensatingCommand() throws Exception {
        ChannelConnectionCommand emptyCommand = new ChannelConnectionCommand();
        ChannelConnectionCommand compensating = emptyCommand.getCompensatingCommand();
        assertTrue(compensating.getAttachCommands().isEmpty());
        assertTrue(compensating.getDetachCommands().isEmpty());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new ChannelConnectionCommand();
        attachCommand1 = new AttachChannelConnectionCommand(new MockDefinition());
        command.add(attachCommand1);

        attachCommand2 = new AttachChannelConnectionCommand(new MockDefinition());
        command.add(attachCommand2);

        attachCommand3 = new AttachChannelConnectionCommand(new MockDefinition());
        command.add(attachCommand3);

        detachCommand1 = new DetachChannelConnectionCommand(new MockDefinition());
        command.add(detachCommand1);

        detachCommand2 = new DetachChannelConnectionCommand(new MockDefinition());
        command.add(detachCommand2);

        detachCommand3 = new DetachChannelConnectionCommand(new MockDefinition());
        command.add(detachCommand3);
    }

    private class MockDefinition extends PhysicalChannelConnectionDefinition {
        private static final long serialVersionUID = 6358317322714807832L;

        public MockDefinition() {
            super(null, null, null);
        }
    }
}
