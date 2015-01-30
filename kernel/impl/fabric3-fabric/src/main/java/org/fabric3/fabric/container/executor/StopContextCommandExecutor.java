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
package org.fabric3.fabric.container.executor;

import javax.xml.namespace.QName;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.fabric.container.command.StopContextCommand;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Stops a component context on a runtime.
 */
@EagerInit
public class StopContextCommandExecutor implements CommandExecutor<StopContextCommand> {
    private CommandExecutorRegistry executorRegistry;
    private ChannelManager channelManager;
    private ScopeContainer compositeScopeContainer;
    private ScopeContainer domainScopeContainer;
    private ContextMonitor monitor;

    public StopContextCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                      @Reference ScopeRegistry scopeRegistry,
                                      @Reference ChannelManager channelManager,
                                      @Monitor ContextMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.channelManager = channelManager;
        this.compositeScopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        this.domainScopeContainer = scopeRegistry.getScopeContainer(Scope.DOMAIN);
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(StopContextCommand.class, this);
    }

    public void execute(StopContextCommand command) throws ContainerException {
        QName deployable = command.getDeployable();
        WorkContextCache.getAndResetThreadWorkContext();
        compositeScopeContainer.stopContext(deployable);
        if (domainScopeContainer != null) {
            // domain scope not available during bootstrap
            domainScopeContainer.stopContext(deployable);
        }
        channelManager.stopContext(deployable);
        if (monitor != null && command.isLog()) {
            monitor.undeployed(deployable);
        }
    }

}

