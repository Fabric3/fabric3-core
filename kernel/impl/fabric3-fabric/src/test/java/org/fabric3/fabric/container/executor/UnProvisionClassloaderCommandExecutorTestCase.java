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
import org.fabric3.fabric.container.builder.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.container.command.UnprovisionClassloaderCommand;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 *
 */
public class UnProvisionClassloaderCommandExecutorTestCase extends TestCase {


    public void testExecute() throws Exception {
        CommandExecutorRegistry registry = EasyMock.createMock(CommandExecutorRegistry.class);
        registry.register(EasyMock.eq(UnprovisionClassloaderCommand.class), EasyMock.isA(UnprovisionClassloaderCommandExecutor.class));
        ClassLoaderBuilder builder = EasyMock.createMock(ClassLoaderBuilder.class);
        builder.destroy(EasyMock.isA(URI.class));
        EasyMock.replay(registry, builder);


        UnprovisionClassloaderCommandExecutor executor = new UnprovisionClassloaderCommandExecutor(registry, builder);
        executor.init();

        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(URI.create("classloader"), true);
        UnprovisionClassloaderCommand command = new UnprovisionClassloaderCommand(definition);
        executor.execute(command);

        EasyMock.verify(registry, builder);
    }

}
