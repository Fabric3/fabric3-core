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

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

public class BuildChannelCommandTestCase extends TestCase {
    private PhysicalChannelDefinition definition;

    public void testCompensatingCommand() throws Exception {
        BuildChannelCommand command = new BuildChannelCommand(definition);
        DisposeChannelCommand compensating = command.getCompensatingCommand();
        assertEquals(definition, compensating.getDefinition());
    }

    public void testEquals() throws Exception {
        BuildChannelCommand command1 = new BuildChannelCommand(definition);
        BuildChannelCommand command2 = new BuildChannelCommand(definition);
        assertEquals(command1, command2);
    }

    protected void setUp() throws Exception {
        super.setUp();
        URI uri = URI.create("channel");
        QName name = new QName("test", "composite");
        definition = new PhysicalChannelDefinition(uri, name);
    }
}
