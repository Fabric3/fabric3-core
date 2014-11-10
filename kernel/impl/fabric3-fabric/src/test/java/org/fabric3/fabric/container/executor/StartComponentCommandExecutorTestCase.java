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
import org.fabric3.fabric.container.command.StartComponentCommand;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;

/**
 *
 */
public class StartComponentCommandExecutorTestCase extends TestCase {

    public void testExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        ComponentManager manager = EasyMock.createMock(ComponentManager.class);
        executorRegistry.register(EasyMock.eq(StartComponentCommand.class), EasyMock.isA(StartComponentCommandExecutor.class));
        Component component = EasyMock.createMock(Component.class);
        component.start();
        EasyMock.expect(manager.getComponent(EasyMock.isA(URI.class))).andReturn(component);
        EasyMock.replay(executorRegistry, manager, component);

        StartComponentCommandExecutor executor = new StartComponentCommandExecutor(manager, executorRegistry);
        executor.init();
        StartComponentCommand command = new StartComponentCommand(URI.create("component"));
        executor.execute(command);
        EasyMock.verify(executorRegistry, manager, component);

    }

}
