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

import junit.framework.TestCase;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

public class DetachWireCommandTestCase extends TestCase {

    public void testCompensatingCommand() throws Exception {
        DetachWireCommand command = new DetachWireCommand();
        PhysicalWireDefinition definition = new PhysicalWireDefinition(null, null, null);
        command.setPhysicalWireDefinition(definition);
        AttachWireCommand compensating = command.getCompensatingCommand();
        assertEquals(definition, compensating.getPhysicalWireDefinition());
    }

    public void testEquals() throws Exception {
        DetachWireCommand command1 = new DetachWireCommand();
        PhysicalWireDefinition definition = new PhysicalWireDefinition(null, null, null);
        command1.setPhysicalWireDefinition(definition);

        DetachWireCommand command2 = new DetachWireCommand();
        command2.setPhysicalWireDefinition(definition);
        assertEquals(command1, command2);
    }
}
