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
package org.fabric3.fabric.runtime;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.ContributionOrder;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.runtime.BootConfiguration;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.runtime.RuntimeState;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.ExtensionsInitialized;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.fabric3.spi.runtime.event.JoinDomainCompleted;
import org.fabric3.spi.runtime.event.RuntimeDestroyed;
import org.fabric3.spi.runtime.event.RuntimeRecover;
import org.fabric3.spi.runtime.event.RuntimeStart;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.fabric3.spi.runtime.event.TransportStart;
import org.fabric3.spi.runtime.event.TransportStop;
import static org.fabric3.api.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.api.host.Names.RUNTIME_DOMAIN_SERVICE_URI;

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

    public void start() throws Fabric3Exception {
        boot();
        load();
        startRuntime();
        startTransports();
    }

    public void boot() throws Fabric3Exception {
        runtime.boot();
        Bootstrapper bootstrapper = new DefaultBootstrapper(configuration);

        // boot runtime domain
        bootstrapper.bootRuntimeDomain();

        // initialize core system components
        bootstrapper.bootSystem();

    }

    public void load() throws Fabric3Exception {
        // load and initialize runtime extension components and the local runtime domain
        loadExtensions();

        EventService eventService = runtime.getComponent(EventService.class);
        eventService.publish(new ExtensionsInitialized());

        // initiate local runtime recovery
        recover(eventService);
    }

    public void startRuntime() {
        EventService eventService = runtime.getComponent(EventService.class);

        eventService.publish(new JoinDomain());
        eventService.publish(new JoinDomainCompleted());

        // signal runtime start
        eventService.publish(new RuntimeStart());
        state = RuntimeState.STARTED;
    }

    public void startTransports() {
        EventService eventService = runtime.getComponent(EventService.class);
        eventService.publish(new TransportStart());
        state = RuntimeState.RUNNING;
    }

    public void shutdown() throws Fabric3Exception {
        if (state == RuntimeState.RUNNING) {
            EventService eventService = runtime.getComponent(EventService.class);
            eventService.publish(new TransportStop());
            eventService.publish(new RuntimeStop());
            RuntimeDestroyed destroyed = new RuntimeDestroyed(); // instantiate event before classloaders are disabled with the call to destroy()
            runtime.destroy();
            eventService.publish(destroyed);
        }
        state = RuntimeState.SHUTDOWN;
    }

    /**
     * Loads runtime extensions.
     *
     * @throws Fabric3Exception if an error loading runtime extensions
     */
    private void loadExtensions() throws Fabric3Exception {
        List<ContributionSource> contributions = configuration.getExtensionContributions();
        ContributionService contributionService = runtime.getComponent(ContributionService.class);
        Domain domain = runtime.getComponent(Domain.class, RUNTIME_DOMAIN_SERVICE_URI);
        try {
            // process manifests and order the contributions
            ContributionOrder order = contributionService.processManifests(contributions);

            order.getBootstrapContributions().forEach(contributionService::processContents);
            // base contributions are deployed in batch since they only rely on boot runtime capabilities
            domain.include(order.getBootstrapContributions());

            order.getBaseContributions().forEach(contributionService::processContents);
            // base contributions are deployed in batch since they only rely on boot runtime capabilities
            domain.include(order.getBaseContributions());

            // Isolated contributions must be introspected and deployed individually as they rely on capabilities provided by another contribution.
            // In this case, the providing contribution must be installed and deployed first, precluding batch deployment
            for (URI uri : order.getIsolatedContributions()) {
                contributionService.processContents(uri);
                domain.include(Collections.singletonList(uri));
            }
        } finally {
            state = RuntimeState.ERROR;
        }
    }

    /**
     * Performs local runtime recovery operations, such as controller recovery and transaction recovery.
     *
     * @param eventService the event service
     * @throws Fabric3Exception if an error performing recovery is encountered
     */
    private void recover(EventService eventService) throws Fabric3Exception {
        Domain domain = runtime.getComponent(Domain.class, APPLICATION_DOMAIN_URI);
        if (domain == null) {
            state = RuntimeState.ERROR;
            String name = APPLICATION_DOMAIN_URI.toString();
            throw new Fabric3Exception("Domain not found: " + name);
        }
        eventService.publish(new RuntimeRecover());
    }
}