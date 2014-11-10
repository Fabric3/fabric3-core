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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.contribution.ContributionNotFoundException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.RemoveException;
import org.fabric3.api.host.contribution.UninstallException;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.fabric.container.command.UnProvisionExtensionsCommand;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.executor.ExecutionException;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.ResolutionException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Undeploys, uninstalls, and removes extension contributions from a runtime.
 */
@EagerInit
public class UnProvisionExtensionsCommandExecutor implements CommandExecutor<UnProvisionExtensionsCommand> {
    private Domain domain;
    private CommandExecutorRegistry commandExecutorRegistry;
    private ContributionService contributionService;
    private ContributionResolver resolver;
    private ProvisionedExtensionTracker tracker;

    public UnProvisionExtensionsCommandExecutor(@Reference(name = "domain") Domain domain,
                                                @Reference CommandExecutorRegistry commandExecutorRegistry,
                                                @Reference ContributionService contributionService,
                                                @Reference ContributionResolver resolver,
                                                @Reference ProvisionedExtensionTracker tracker) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.contributionService = contributionService;
        this.domain = domain;
        this.resolver = resolver;
        this.tracker = tracker;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(UnProvisionExtensionsCommand.class, this);
    }

    public synchronized void execute(UnProvisionExtensionsCommand command) throws ContainerException {
        // compile the list of extensions 
        List<URI> uninstall = new ArrayList<>();
        for (URI uri : command.getExtensionUris()) {
            int count = tracker.decrement(uri);
            if (count == 0) {
                try {
                    // no longer in use, undeploy and uninstall the extension
                    domain.undeploy(uri, false);
                    uninstall.add(uri);
                    resolver.release(uri);
                } catch (DeploymentException | ResolutionException e) {
                    throw new ExecutionException(e);
                }
            }
        }
        try {
            contributionService.uninstall(uninstall);
            contributionService.remove(uninstall);
        } catch (ContributionNotFoundException | UninstallException | RemoveException e) {
            throw new ExecutionException(e);
        }
    }

}