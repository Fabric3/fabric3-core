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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.contribution.ContributionException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.FileContributionSource;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.fabric.container.command.ProvisionExtensionsCommand;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.executor.ExecutionException;
import org.fabric3.spi.contribution.ContributionResolver;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Provisions and installs extension contributions.
 */
@EagerInit
public class ProvisionExtensionsCommandExecutor implements CommandExecutor<ProvisionExtensionsCommand> {
    private CommandExecutorRegistry commandExecutorRegistry;
    private ContributionService contributionService;
    private Domain domain;
    private ContributionResolver resolver;
    private ProvisionedExtensionTracker tracker;

    public ProvisionExtensionsCommandExecutor(@Reference(name = "domain") Domain domain,
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
        commandExecutorRegistry.register(ProvisionExtensionsCommand.class, this);
    }

    public void execute(ProvisionExtensionsCommand command) throws ContainerException {
        try {
            List<URI> stored = new ArrayList<>();
            for (URI uri : command.getExtensionUris()) {
                if (contributionService.exists(uri)) {
                    // extension already provisioned
                    continue;
                }
                URL url = resolver.resolve(uri);
                ContributionSource source = new FileContributionSource(uri, url, 0, true);
                contributionService.store(source);
                stored.add(uri);
                tracker.increment(uri);
            }
            if (stored.isEmpty()) {
                return;
            }
            contributionService.install(stored);
            domain.include(stored);
        } catch (DeploymentException | ContributionException e) {
            throw new ExecutionException(e);
        }
    }

}
