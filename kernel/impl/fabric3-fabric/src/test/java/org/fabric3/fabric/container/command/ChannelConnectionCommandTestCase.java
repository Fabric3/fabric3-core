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
