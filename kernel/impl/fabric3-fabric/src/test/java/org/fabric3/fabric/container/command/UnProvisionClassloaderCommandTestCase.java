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

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

public class UnProvisionClassloaderCommandTestCase extends TestCase {
    private UnprovisionClassloaderCommand command;

    public void testCompensatingCommand() throws Exception {
        ProvisionClassloaderCommand compensating = command.getCompensatingCommand();
        assertEquals(command.getClassLoaderDefinition(), compensating.getClassLoaderDefinition());
    }

    public void testEquals() throws Exception {
        UnprovisionClassloaderCommand command2 = new UnprovisionClassloaderCommand(new MockDefinition());
        assertEquals(command2, command);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new UnprovisionClassloaderCommand(new MockDefinition());
    }

    private class MockDefinition extends PhysicalClassLoaderDefinition {
        private static final long serialVersionUID = 6358317322714807832L;

        public MockDefinition() {
            super(URI.create("classloader"), true);
        }
    }
}
