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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.executor;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.command.DetachWireCommand;
import org.fabric3.fabric.container.command.DetachWireCommandExecutor;
import org.fabric3.fabric.container.builder.Connector;
import org.fabric3.fabric.container.command.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalWire;

/**
 *
 */
public class DetachWireCommandExecutorTestCase extends TestCase {

    public void testAttachExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        Connector connector = EasyMock.createMock(Connector.class);
        executorRegistry.register(EasyMock.eq(DetachWireCommand.class), EasyMock.isA(DetachWireCommandExecutor.class));
        connector.disconnect(EasyMock.isA(PhysicalWire.class));
        EasyMock.replay(executorRegistry, connector);

        DetachWireCommandExecutor executor = new DetachWireCommandExecutor(executorRegistry, connector);
        executor.init();
        PhysicalWire physicalWire = new PhysicalWire(null, null, null);
        DetachWireCommand command = new DetachWireCommand();
        command.setPhysicalWireDefinition(physicalWire);
        executor.execute(command);
        EasyMock.verify(executorRegistry, connector);

    }

}
