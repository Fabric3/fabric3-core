/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
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