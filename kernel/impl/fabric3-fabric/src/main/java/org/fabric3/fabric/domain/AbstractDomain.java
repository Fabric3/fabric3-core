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
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.domain.ContributionNotFoundException;
import org.fabric3.api.host.domain.ContributionNotInstalledException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.Deployment;
import org.fabric3.spi.domain.generator.Generator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Base class for a domain.
 */
public abstract class AbstractDomain implements Domain {

    protected Deployer deployer;
    protected Generator generator;

    protected List<DeployListener> listeners;

    protected MetaDataStore metadataStore;
    protected LogicalComponentManager logicalComponentManager;
    protected LogicalModelInstantiator logicalModelInstantiator;
    protected Collector collector;
    protected ContributionHelper contributionHelper;
    protected HostInfo info;

    // The service for allocating to remote zones. Domain subtypes may optionally inject this service if they support distributed domains.
    protected Allocator allocator;

    /**
     * Constructor.
     *
     * @param metadataStore      the store for resolving contribution artifacts
     * @param lcm                the manager for logical components
     * @param generator          the physical model generator
     * @param instantiator       the logical model instantiator
     * @param deployer           the service for sending deployment commands
     * @param collector          the collector for undeploying components
     * @param contributionHelper the contribution helper
     * @param info               the host info
     */
    public AbstractDomain(MetaDataStore metadataStore,
                          LogicalComponentManager lcm,
                          Generator generator,
                          LogicalModelInstantiator instantiator,
                          Deployer deployer,
                          Collector collector,
                          ContributionHelper contributionHelper,
                          HostInfo info) {
        this.metadataStore = metadataStore;
        this.generator = generator;
        this.logicalModelInstantiator = instantiator;
        this.logicalComponentManager = lcm;
        this.deployer = deployer;
        this.collector = collector;
        this.contributionHelper = contributionHelper;
        this.info = info;
        listeners = Collections.emptyList();
    }

    public synchronized void include(QName deployable) throws ContainerException {
        Composite wrapper = createWrapper(deployable);
        for (DeployListener listener : listeners) {
            listener.onDeploy(deployable);
        }
        instantiateAndDeploy(wrapper, false);
        for (DeployListener listener : listeners) {
            listener.onDeployCompleted(deployable);
        }
    }

    public synchronized void include(Composite composite, boolean simulated) throws ContainerException {
        QName name = composite.getName();
        for (DeployListener listener : listeners) {
            listener.onDeploy(name);
        }
        instantiateAndDeploy(composite, simulated);
        for (DeployListener listener : listeners) {
            listener.onDeployCompleted(name);
        }
    }

    public synchronized void include(List<URI> uris) throws ContainerException {
        include(uris, false);
    }

    public synchronized void undeploy(URI uri, boolean force) throws ContainerException {
        Contribution contribution = metadataStore.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        if (deployables.isEmpty()) {
            return;
        }
        // reverse the deployables
        List<QName> names = new ArrayList<>();
        ListIterator<Deployable> iter = deployables.listIterator(deployables.size());
        while (iter.hasPrevious()) {
            names.add(iter.previous().getName());
        }
        for (QName deployable : names) {
            if (!contribution.getLockOwners().contains(deployable)) {
                throw new CompositeNotDeployedException("Composite is not deployed: " + deployable);
            }
        }

        for (QName deployable : names) {
            for (DeployListener listener : listeners) {
                listener.onUndeploy(deployable);
            }
        }
        for (DeployListener listener : listeners) {
            listener.onUnDeploy(uri);
        }
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        for (QName deployable : names) {
            collector.markForCollection(deployable, domain);
        }
        Deployment deployment = generator.generate(domain);
        collector.collect(domain);
        try {
            deployer.deploy(deployment);
        } catch (ContainerException e) {
            if (!force) {
                throw e;
            }
            // force undeployment in effect: ignore deployment exceptions
        }
        for (QName deployable : names) {
            contribution.releaseLock(deployable);
        }
        logicalComponentManager.replaceRootComponent(domain);
        for (QName deployable : names) {
            for (DeployListener listener : listeners) {
                listener.onUndeployCompleted(deployable);
            }
        }
        for (DeployListener listener : listeners) {
            listener.onUnDeployCompleted(uri);
        }
    }

    public synchronized void undeploy(Composite composite, boolean simulated) throws ContainerException {
        QName deployable = composite.getName();
        for (DeployListener listener : listeners) {
            listener.onUndeploy(deployable);
        }

        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        collector.markForCollection(deployable, domain);
        if (!simulated) {
            Deployment deployment = generator.generate(domain);
            collector.collect(domain);
            deployer.deploy(deployment);
        } else {
            collector.collect(domain);
        }
        URI uri = composite.getContributionUri();
        Contribution contribution = metadataStore.find(uri);
        contribution.releaseLock(deployable);
        logicalComponentManager.replaceRootComponent(domain);
        for (DeployListener listener : listeners) {
            listener.onUndeployCompleted(deployable);
        }
    }

    public synchronized void deactivateDefinitions(URI uri) throws ContainerException {
        Contribution contribution = metadataStore.find(uri);
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + uri);
        }
    }

    /**
     * Selects bindings for references targeted to remote services for a set of components being deployed by delegating to a BindingSelector.
     *
     * @param domain the domain component
     */
    protected void selectBinding(LogicalCompositeComponent domain) throws ContainerException {
        // no-op
    }

    /**
     * Include all deployables contained in the list of contributions in the domain.
     *
     * @param uris    the contributions to deploy
     * @param recover true if this is a recovery operation
     * @throws ContainerException if an error is encountered during inclusion
     */
    private synchronized void include(List<URI> uris, boolean recover) throws ContainerException {
        Set<Contribution> contributions = contributionHelper.findContributions(uris);
        List<Composite> deployables = contributionHelper.getDeployables(contributions);
        // notify listeners
        for (URI uri : uris) {
            for (DeployListener listener : listeners) {
                listener.onDeploy(uri);
            }
        }
        for (Composite deployable : deployables) {
            for (DeployListener listener : listeners) {
                listener.onDeploy(deployable.getName());
            }
        }
        instantiateAndDeploy(deployables, contributions, recover);
        for (Composite deployable : deployables) {
            for (DeployListener listener : listeners) {
                listener.onDeployCompleted(deployable.getName());
            }
        }
        for (URI uri : uris) {
            for (DeployListener listener : listeners) {
                listener.onDeployCompleted(uri);
            }
        }
    }

    /**
     * Creates a wrapper used to include a composite at the domain level. The wrapper is thrown away during the inclusion.
     *
     * @param deployable the deployable being included
     * @return the composite wrapper
     * @throws ContainerException if there is an error creating the composite wrapper
     */
    private Composite createWrapper(QName deployable) throws ContainerException {
        Composite composite = contributionHelper.findComposite(deployable);
        // In order to include a composite at the domain level, it must first be wrapped in a composite that includes it.
        // This wrapper is thrown away during the inclusion.
        Composite wrapper = new Composite(deployable);
        Include include = new Include();
        include.setName(deployable);
        include.setIncluded(composite);
        wrapper.add(include);
        return wrapper;
    }

    /**
     * Instantiates and optionally deploys deployables from a set of contributions. Deployment is performed if recovery mode is false or the runtime is
     * operating in single VM mode. When recovering in a distributed domain, the components contained in the deployables will be instantiated but not deployed
     * to zones. This is because the domain can run headless (i.e. without a controller) and may already be hosting deployed components.
     *
     * @param deployables   the deployables
     * @param contributions the contributions to deploy
     * @param recover       true if recovery mode is enabled
     * @throws ContainerException if an error occurs during instantiation or deployment
     */
    private void instantiateAndDeploy(List<Composite> deployables, Set<Contribution> contributions, boolean recover) throws ContainerException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        for (Contribution contribution : contributions) {
            if (ContributionState.INSTALLED != contribution.getState()) {
                throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
            }
        }

        // lock the contributions
        contributionHelper.lock(contributions);
        try {
            InstantiationContext context = logicalModelInstantiator.include(deployables, domain);
            if (context.hasErrors()) {
                contributionHelper.releaseLocks(contributions);
                throw new AssemblyException(context.getErrors());
            }
            if (!recover || RuntimeMode.VM == info.getRuntimeMode()) {
                // in single VM mode, recovery includes deployment
                allocateAndDeploy(domain);
            } else {
                allocate(domain);
                // Select bindings
                selectBinding(domain);
                collector.markAsProvisioned(domain);
            }

            logicalComponentManager.replaceRootComponent(domain);

        } catch (ContainerException e) {
            // release the contribution locks if there was an error
            contributionHelper.releaseLocks(contributions);
            throw e;
        }
    }

    /**
     * Instantiates and deploys the given composite.
     *
     * @param composite the composite to instantiate and deploy
     * @param simulated true if the deployment is simulated
     * @throws ContainerException if a deployment error occurs
     */
    private void instantiateAndDeploy(Composite composite, boolean simulated) throws ContainerException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        QName name = composite.getName();
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Composite> element = metadataStore.find(Composite.class, symbol);
        if (element == null) {
            throw new ContainerException("Composite not found in metadata store: " + name);
        }
        Contribution contribution = element.getResource().getContribution();
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
        }

        // check if the deployable has already been deployed by querying the lock owners
        if (contribution.getLockOwners().contains(name)) {
            throw new CompositeAlreadyDeployedException("Composite has already been deployed: " + name);
        }
        // lock the contribution
        contribution.acquireLock(name);
        InstantiationContext context = logicalModelInstantiator.include(composite, domain);
        if (context.hasErrors()) {
            if (!simulated) {
                contribution.releaseLock(name);
                throw new AssemblyException(context.getErrors());
            }
        }
        if (!simulated) {
            try {
                allocateAndDeploy(domain);
            } catch (ContainerException e) {
                // release the contribution lock if there was an error
                if (contribution.getLockOwners().contains(name)) {
                    contribution.releaseLock(name);
                }
                throw e;
            }
            logicalComponentManager.replaceRootComponent(domain);
        } else {
            collector.markAsProvisioned(domain);
            logicalComponentManager.replaceRootComponent(domain);
            if (context.hasErrors()) {
                throw new AssemblyException(context.getErrors());
            }
        }
    }

    /**
     * Allocates and deploys new components in the domain.
     *
     * @param domain the domain component
     * @throws ContainerException if an error is encountered during deployment
     */
    private void allocateAndDeploy(LogicalCompositeComponent domain) throws ContainerException {
        // Allocate the components to runtime nodes
        allocate(domain);
        // Select bindings
        selectBinding(domain);
        // generate and provision any new components and new wires
        Deployment deployment = generator.generate(domain);
        collector.markAsProvisioned(domain);
        deployer.deploy(deployment);
    }

    /**
     * Delegates to the Allocator to determine which runtimes to deploy the given collection of components to.
     *
     * @param domain the domain component
     * @throws AllocationException if an allocation error occurs
     */
    private void allocate(LogicalCompositeComponent domain) throws AllocationException {
        if (allocator == null) {
            // allocator is an optional extension
            return;
        }
        for (LogicalResource<?> resource : domain.getResources()) {
            if (resource.getState() == LogicalState.NEW) {
                allocator.allocate(resource);
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            if (channel.getState() == LogicalState.NEW) {
                allocator.allocate(channel);
            }
        }
        Collection<LogicalComponent<?>> components = domain.getComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                allocator.allocate(component);
            }
        }
    }

}
