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

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.command.StopComponentCommand;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;

/**
 *
 */
public class StopComponentCommandExecutorTestCase extends TestCase {

    public void testExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        ComponentManager manager = EasyMock.createMock(ComponentManager.class);
        executorRegistry.register(EasyMock.eq(StopComponentCommand.class), EasyMock.isA(StopComponentCommandExecutor.class));
        Component component = EasyMock.createMock(Component.class);
        component.stop();
        EasyMock.expect(manager.getComponent(EasyMock.isA(URI.class))).andReturn(component);
        EasyMock.replay(executorRegistry, manager, component);

        StopComponentCommandExecutor executor = new StopComponentCommandExecutor(manager, executorRegistry);
        executor.init();
        StopComponentCommand command = new StopComponentCommand(URI.create("component"));
        executor.execute(command);
        EasyMock.verify(executorRegistry, manager, component);
    }

}
