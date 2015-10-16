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
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.generator.Deployment;
import org.fabric3.fabric.domain.generator.Generator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Base class for a domain.
 */
public abstract class AbstractDomain implements Domain {

    protected Deployer deployer;
    protected Generator generator;

    protected MetaDataStore metadataStore;
    protected LogicalComponentManager logicalComponentManager;
    protected LogicalModelInstantiator logicalModelInstantiator;
    protected Collector collector;
    protected ContributionHelper contributionHelper;
    protected HostInfo info;

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
    }

    public synchronized void include(Composite composite) throws Fabric3Exception {
        instantiateAndDeploy(composite);
    }

    public synchronized void include(List<URI> uris) throws Fabric3Exception {
        include(uris, false);
    }

    public synchronized void undeploy(URI uri) throws Fabric3Exception {
        Contribution contribution = metadataStore.find(uri);
        if (contribution == null) {
            throw new Fabric3Exception("Contribution not found: " + uri);
        }
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        if (deployables.isEmpty()) {
            return;
        }
        LogicalCompositeComponent domain = logicalComponentManager.getDomainComposite();
        collector.markForCollection(uri, domain);
        Deployment deployment = generator.generate(domain);
        collector.collect(domain);
        deployer.deploy(deployment);
        contribution.undeploy();
    }

    public synchronized void undeploy(Composite composite) throws Fabric3Exception {
        LogicalCompositeComponent domain = logicalComponentManager.getDomainComposite();
        URI contributionUri = composite.getContributionUri();
        collector.markForCollection(contributionUri, domain);
        Deployment deployment = generator.generate(domain);
        collector.collect(domain);
        deployer.deploy(deployment);
        Contribution contribution = metadataStore.find(contributionUri);
        contribution.undeploy();
    }

    /**
     * Include all deployables contained in the list of contributions in the domain.
     *
     * @param uris    the contributions to deploy
     * @param recover true if this is a recovery operation
     * @throws Fabric3Exception if an error is encountered during inclusion
     */
    private synchronized void include(List<URI> uris, boolean recover) throws Fabric3Exception {
        Set<Contribution> contributions = contributionHelper.findContributions(uris);
        List<Composite> deployables = contributionHelper.getDeployables(contributions);
        instantiateAndDeploy(deployables, contributions, recover);
    }

    /**
     * Instantiates and optionally deploys deployables from a set of contributions. Deployment is performed if recovery mode is false or the runtime is
     * operating in single VM mode. When recovering in a distributed domain, the components contained in the deployables will be instantiated but not deployed
     * to zones. This is because the domain can run headless (i.e. without a controller) and may already be hosting deployed components.
     *
     * @param deployables   the deployables
     * @param contributions the contributions to deploy
     * @param recover       true if recovery mode is enabled
     * @throws Fabric3Exception if an error occurs during instantiation or deployment
     */
    private void instantiateAndDeploy(List<Composite> deployables, Set<Contribution> contributions, boolean recover) throws Fabric3Exception {
        LogicalCompositeComponent domain = logicalComponentManager.getDomainComposite();

        for (Contribution contribution : contributions) {
            if (ContributionState.STORED == contribution.getState()) {
                throw new Fabric3Exception("Contribution is not installed: " + contribution.getUri());
            }
        }

        InstantiationContext context = logicalModelInstantiator.include(deployables, domain);
        if (context.hasErrors()) {
            throw new AssemblyException(context.getErrors());
        }
        if (!recover || RuntimeMode.VM == info.getRuntimeMode()) {
            // in single VM mode, recovery includes deployment
            deploy(domain);
        } else {
            collector.markAsProvisioned(domain);
        }

        contributions.forEach(Contribution::deploy);
    }

    /**
     * Instantiates and deploys the given composite.
     *
     * @param composite the composite to instantiate and deploy
     * @throws Fabric3Exception if a deployment error occurs
     */
    private void instantiateAndDeploy(Composite composite) throws Fabric3Exception {
        LogicalCompositeComponent domain = logicalComponentManager.getDomainComposite();

        QName name = composite.getName();
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Composite> element = metadataStore.find(Composite.class, symbol);
        if (element == null) {
            throw new Fabric3Exception("Composite not found in metadata store: " + name);
        }
        Contribution contribution = element.getResource().getContribution();
        if (ContributionState.STORED == contribution.getState()) {
            throw new Fabric3Exception("Contribution is not installed: " + contribution.getUri());
        }

        InstantiationContext context = logicalModelInstantiator.include(composite, domain);
        if (context.hasErrors()) {
            throw new AssemblyException(context.getErrors());
        }
        deploy(domain);
        contribution.deploy();
    }

    /**
     * Allocates and deploys new components in the domain.
     *
     * @param domain the domain component
     * @throws Fabric3Exception if an error is encountered during deployment
     */
    private void deploy(LogicalCompositeComponent domain) throws Fabric3Exception {
        // generate and provision any new components and new wires
        Deployment deployment = generator.generate(domain);
        collector.markAsProvisioned(domain);
        deployer.deploy(deployment);
    }

}
