/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import static org.fabric3.fabric.runtime.FabricNames.EVENT_SERVICE_URI;
import org.fabric3.fabric.runtime.bootstrap.Bootstrapper;
import org.fabric3.fabric.runtime.bootstrap.DefaultBootstrapper;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.host.Names.RUNTIME_DOMAIN_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.RuntimeState;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.spi.event.DomainRecover;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.ExtensionsInitialized;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeRecover;
import org.fabric3.spi.event.RuntimeStart;

/**
 * Default implementation of the RuntimeCoordinator.
 *
 * @version $Rev$ $Date$
 */
public class DefaultCoordinator implements RuntimeCoordinator {
    private RuntimeState state = RuntimeState.UNINITIALIZED;
    private Fabric3Runtime<?> runtime;
    private Bootstrapper bootstrapper;
    private ClassLoader bootClassLoader;
    private Map<String, String> exportedPackages;
    private List<ContributionSource> extensionContributions;
    private List<ContributionSource> userContributions;
    private List<ComponentRegistration> registrations;
    private URL systemCompositeUrl;
    private Document systemConfig;

    public DefaultCoordinator(BootConfiguration configuration) {
        bootstrapper = new DefaultBootstrapper();
        runtime = configuration.getRuntime();
        bootClassLoader = configuration.getBootClassLoader();
        exportedPackages = configuration.getExportedPackages();
        extensionContributions = configuration.getExtensionContributions();
        userContributions = configuration.getUserContributions();
        registrations = configuration.getRegistrations();
        systemCompositeUrl = configuration.getSystemCompositeUrl();
        systemConfig = configuration.getSystemConfig();
    }

    public RuntimeState getState() {
        return state;
    }

    public void start() throws InitializationException {
        bootPrimordial();

        // load and initialize runtime extension components and the local runtime domain
        loadExtensions();

        EventService eventService = runtime.getComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new ExtensionsInitialized());

        recover(eventService);
        joinDomain(eventService);

        // starts the runtime by publishing a start event
        eventService.publish(new RuntimeStart());
        state = RuntimeState.STARTED;
    }

    public void shutdown() throws ShutdownException {
        if (state == RuntimeState.STARTED) {
            runtime.destroy();
        }
        state = RuntimeState.SHUTDOWN;
    }

    /**
     * Boots the runtime domain and primordial components.
     *
     * @throws InitializationException if an error booting the runtime domain is encountered
     */
    private void bootPrimordial() throws InitializationException {
        runtime.boot();
        bootstrapper.bootRuntimeDomain(runtime, systemCompositeUrl, systemConfig, bootClassLoader, registrations, exportedPackages);
    }

    /**
     * Loads runtime extensions.
     *
     * @throws InitializationException if an error loading runtime extensions
     */
    private void loadExtensions() throws InitializationException {
        // initialize core system components
        bootstrapper.bootSystem();
        // install extensions
        List<URI> uris = installContributions(extensionContributions);
        // deploy extensions
        deploy(uris);
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
            throw new InitializationException("Domain not found: " + name, name);
        }
        // install user contributions - they will be deployed when the domain recovers
        installContributions(userContributions);
        eventService.publish(new RuntimeRecover());
    }

    /**
     * Synchronizes the runtime with the domain
     *
     * @param eventService the event service
     */
    private void joinDomain(EventService eventService) {
        eventService.publish(new JoinDomain());
        eventService.publish(new DomainRecover());
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
            return contributionService.contribute(sources);
        } catch (ContributionException e) {
            state = RuntimeState.ERROR;
            throw new ExtensionInitializationException("Error contributing extensions", e);
        }
    }

    /**
     * Deploys a collection of contributions.
     *
     * @param contributionUris the contribution URIs
     * @throws InitializationException if a deployment exception occurs
     */
    private void deploy(List<URI> contributionUris) throws InitializationException {
        try {
            Domain domain = runtime.getComponent(Domain.class, RUNTIME_DOMAIN_SERVICE_URI);
            domain.include(contributionUris);
        } catch (DeploymentException e) {
            state = RuntimeState.ERROR;
            throw new ExtensionInitializationException("Error deploying extensions", e);
        }
    }

}