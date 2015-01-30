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
import org.fabric3.fabric.container.command.DetachChannelConnectionCommand;
import org.fabric3.spi.container.builder.ChannelConnector;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

/**
 *
 */
public class DetachChannelConnectionCommandExecutorTestCase extends TestCase {


    public void testExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        ChannelConnector connector = EasyMock.createMock(ChannelConnector.class);
        executorRegistry.register(EasyMock.eq(DetachChannelConnectionCommand.class), EasyMock.isA(DetachChannelConnectionCommandExecutor.class));
        connector.disconnect(EasyMock.isA(PhysicalChannelConnectionDefinition.class));
        EasyMock.replay(executorRegistry, connector);

        DetachChannelConnectionCommandExecutor executor = new DetachChannelConnectionCommandExecutor(executorRegistry, connector);
        executor.init();
        PhysicalChannelConnectionDefinition definition = new PhysicalChannelConnectionDefinition(null, null, null);
        DetachChannelConnectionCommand command = new DetachChannelConnectionCommand(definition);
        executor.execute(command);
        EasyMock.verify(executorRegistry, connector);

    }
}
