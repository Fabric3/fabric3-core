/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.fabric.runtime;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionOrder;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.RuntimeState;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.spi.event.DomainRecover;
import org.fabric3.spi.event.DomainRecovered;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.ExtensionsInitialized;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeRecover;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.event.RuntimeStop;

import static org.fabric3.fabric.runtime.FabricNames.EVENT_SERVICE_URI;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.host.Names.RUNTIME_DOMAIN_SERVICE_URI;

/**
 * Default implementation of the RuntimeCoordinator.
 */
public class DefaultCoordinator implements RuntimeCoordinator {
    private RuntimeState state = RuntimeState.UNINITIALIZED;
    private BootConfiguration configuration;
    private Fabric3Runtime runtime;

    public DefaultCoordinator(BootConfiguration configuration) {
        this.configuration = configuration;
        runtime = configuration.getRuntime();
    }

    public RuntimeState getState() {
        return state;
    }

    public void start() throws InitializationException {
        prepare();
        joinAndStart();
    }

    public void prepare() throws InitializationException {
        // boot primordial services
        runtime.boot();

        Bootstrapper bootstrapper = new DefaultBootstrapper(configuration);

        // boot runtime domain
        bootstrapper.bootRuntimeDomain();

        // initialize core system components
        bootstrapper.bootSystem();

        // load and initialize runtime extension components and the local runtime domain
        loadExtensions();

        EventService eventService = runtime.getComponent(EventService.class);
        eventService.publish(new ExtensionsInitialized());

        // initiate local runtime recovery
        recover(eventService);

    }

    public void joinAndStart() {
        EventService eventService = runtime.getComponent(EventService.class);
        eventService.publish(new JoinDomain());

        // initiate domain recovery
        eventService.publish(new DomainRecover());

        // signal domain finished recovery
        eventService.publish(new DomainRecovered());

        // signal runtime start
        eventService.publish(new RuntimeStart());
        state = RuntimeState.STARTED;
    }


    public void shutdown() throws ShutdownException {
        if (state == RuntimeState.STARTED) {
            EventService eventService = runtime.getComponent(EventService.class);
            eventService.publish(new RuntimeStop());
            runtime.destroy();
        }
        state = RuntimeState.SHUTDOWN;
    }


    /**
     * Loads runtime extensions.
     *
     * @throws InitializationException if an error loading runtime extensions
     */
    private void loadExtensions() throws InitializationException {
        List<ContributionSource> contributions = configuration.getExtensionContributions();
        ContributionService contributionService = runtime.getComponent(ContributionService.class);
        Domain domain = runtime.getComponent(Domain.class, RUNTIME_DOMAIN_SERVICE_URI);
        try {
            // process manifests and order the contributions
            ContributionOrder order = contributionService.processManifests(contributions);
            for (URI uri : order.getBaseContributions()) {
                contributionService.processContents(uri);
            }
            // base contributions are deployed in batch since they only rely on boot runtime capabilities
            domain.include(order.getBaseContributions());

            // Isolated contributions must be introspected and deployed individually as they rely on capabilities provided by another contribution.
            // In this case, the providing contribution must be installed and deployed first, precluding batch deployment
            for (URI uri : order.getIsolatedContributions()) {
                contributionService.processContents(uri);
                domain.include(Collections.singletonList(uri));
            }
        } catch (InstallException e) {
            throw new InitializationException(e);
        } catch (StoreException e) {
            throw new InitializationException(e);
        } catch (ContributionNotFoundException e) {
            throw new InitializationException(e);
        } catch (DeploymentException e) {
            state = RuntimeState.ERROR;
            throw new ExtensionInitializationException("Error deploying extensions", e);
        }
    }

    /**
     * Performs local runtime recovery operations, such as controller recovery and transaction recovery.
     *
     * @param eventService the event service
     * @throws InitializationException if an error performing recovery is encountered
     */
    private void recover(EventService eventService) throws InitializationException {
        Domain domain = runtime.getComponent(Domain.class, APPLICATION_DOMAIN_URI);
        if (domain == null) {
            state = RuntimeState.ERROR;
            String name = APPLICATION_DOMAIN_URI.toString();
            throw new InitializationException("Domain not found: " + name);
        }
        // install user contributions - they will be deployed when the domain recovers
        List<ContributionSource> contributions = configuration.getUserContributions();
        if (!contributions.isEmpty()) {
            installContributions(contributions);
        }
        eventService.publish(new RuntimeRecover());
    }

    /**
     * Installs a collection of contributions.
     *
     * @param sources the contribution sources
     * @return the list of installed contribution URIs
     * @throws InitializationException if an installation error occurs
     */
    private List<URI> installContributions(List<ContributionSource> sources) throws InitializationException {
        try {
            ContributionService contributionService = runtime.getComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            // install the contributions
            List<URI> stored = contributionService.store(sources);
            return contributionService.install(stored);
        } catch (ContributionException e) {
            state = RuntimeState.ERROR;
            throw new ExtensionInitializationException("Error contributing extensions", e);
        }
    }
}