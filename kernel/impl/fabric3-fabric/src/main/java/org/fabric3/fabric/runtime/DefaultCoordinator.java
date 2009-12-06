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
import java.util.List;
import java.util.Map;

import static org.fabric3.fabric.runtime.FabricNames.EVENT_SERVICE_URI;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.host.Names.RUNTIME_DOMAIN_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.RuntimeState;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.spi.event.DomainRecover;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.event.RuntimeRecover;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.policy.PolicyActivationException;

/**
 * Default implementation of a RuntimeLifecycleCoordinator.
 *
 * @version $Rev$ $Date$
 */
public class DefaultCoordinator implements RuntimeLifecycleCoordinator {
    private RuntimeState state = RuntimeState.INSTANTIATED;
    private Fabric3Runtime<?> runtime;
    private Bootstrapper bootstrapper;
    private ClassLoader bootClassLoader;
    private Map<String, String> exportedPackages;
    private List<ContributionSource> extensionContributions;
    private List<ContributionSource> userContributions;
    private List<ComponentRegistration> registrations;

    public RuntimeState getState() {
        return state;
    }

    public void setConfiguration(BootConfiguration configuration) {
        runtime = configuration.getRuntime();
        bootstrapper = configuration.getBootstrapper();
        bootClassLoader = configuration.getBootClassLoader();
        exportedPackages = configuration.getExportedPackages();
        extensionContributions = configuration.getExtensionContributions();
        userContributions = configuration.getUserContributions();
        registrations = configuration.getRegistrations();
    }

    public void bootPrimordial() throws InitializationException {
        if (state != RuntimeState.INSTANTIATED) {
            throw new IllegalStateException("Not in INSTANTIATED state");
        }
        runtime.boot();
        bootstrapper.bootRuntimeDomain(runtime, bootClassLoader, registrations, exportedPackages);
        state = RuntimeState.PRIMORDIAL;
    }

    public void initialize() throws InitializationException {

        if (state != RuntimeState.PRIMORDIAL) {
            throw new IllegalStateException("Not in PRIMORDIAL state");
        }
        // initialize core system components
        bootstrapper.bootSystem();

        try {
            // install extensions
            List<URI> uris = installContributions(extensionContributions);
            // deploy extensions
            deploy(uris);
        } catch (PolicyActivationException e) {
            throw new InitializationException(e);
        }

        state = RuntimeState.INITIALIZED;
    }

    public void recover() throws InitializationException {
        if (state != RuntimeState.INITIALIZED) {
            throw new IllegalStateException("Not in INITIALIZED state");
        }
        Domain domain = runtime.getComponent(Domain.class, APPLICATION_DOMAIN_URI);
        if (domain == null) {
            String name = APPLICATION_DOMAIN_URI.toString();
            throw new InitializationException("Domain not found: " + name, name);
        }
        // install user contibutions - they will be deployed when the domain recovers
        installContributions(userContributions);
        EventService eventService = runtime.getComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeRecover());
        state = RuntimeState.RECOVERED;
    }

    public void joinDomain(final long timeout) {
        if (state != RuntimeState.RECOVERED) {
            throw new IllegalStateException("Not in RECOVERED state");
        }
        EventService eventService = runtime.getComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new JoinDomain());
        eventService.publish(new DomainRecover());
        state = RuntimeState.JOINED_DOMAIN;
    }

    public void start() throws InitializationException {
        if (state != RuntimeState.JOINED_DOMAIN) {
            throw new IllegalStateException("Not in JOINED_DOMAIN state");
        }
        // starts the runtime by publishing a start event
        EventService eventService = runtime.getComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeStart());
        state = RuntimeState.STARTED;
    }

    public void shutdown() throws ShutdownException {
        if (state == RuntimeState.STARTED) {
            runtime.destroy();
            state = RuntimeState.SHUTDOWN;
        }
    }

    private List<URI> installContributions(List<ContributionSource> sources) throws InitializationException {
        try {
            ContributionService contributionService = runtime.getComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            // install the contributions
            return contributionService.contribute(sources);

        } catch (ContributionException e) {
            throw new ExtensionInitializationException("Error contributing extensions", e);
        }
    }

    private void deploy(List<URI> contributionUris) throws InitializationException, PolicyActivationException {
        try {
            Domain domain = runtime.getComponent(Domain.class, RUNTIME_DOMAIN_SERVICE_URI);
            domain.include(contributionUris, false);
        } catch (DeploymentException e) {
            throw new ExtensionInitializationException("Error deploying extensions", e);
        }
    }

}