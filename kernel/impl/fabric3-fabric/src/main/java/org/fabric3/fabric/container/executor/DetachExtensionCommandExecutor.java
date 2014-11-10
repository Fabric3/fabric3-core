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

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.fabric.container.command.DetachExtensionCommand;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Detaches a parent extension classloader from another classloader.
 */
@EagerInit
public class DetachExtensionCommandExecutor implements CommandExecutor<DetachExtensionCommand> {

    private HostInfo info;
    private CommandExecutorRegistry commandExecutorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    @Constructor
    public DetachExtensionCommandExecutor(@Reference HostInfo info,
                                          @Reference CommandExecutorRegistry commandExecutorRegistry,
                                          @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.info = info;
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(DetachExtensionCommand.class, this);
    }

    public void execute(DetachExtensionCommand command) throws ContainerException {
        if (!info.supportsClassLoaderIsolation()) {
            return;
        }
        URI contributionUri = command.getContribution();
        URI providerUri = command.getProvider();
        // note: casts are safe as all extension and provider classloaders are multi-parent
        MultiParentClassLoader contributionCl = (MultiParentClassLoader) classLoaderRegistry.getClassLoader(contributionUri);
        MultiParentClassLoader providerCl = (MultiParentClassLoader) classLoaderRegistry.getClassLoader(providerUri);
        contributionCl.removeExtensionClassLoader(providerCl);
    }
}