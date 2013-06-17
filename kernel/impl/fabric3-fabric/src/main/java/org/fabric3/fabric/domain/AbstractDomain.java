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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.ContributionNotFoundException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.domain.DomainJournal;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.generator.policy.PolicyActivationException;
import org.fabric3.spi.generator.policy.PolicyAttacher;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.generator.policy.PolicyResolutionException;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.CopyUtil;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Base class for a domain.
 */
public abstract class AbstractDomain implements Domain {
    private static final String SYNTHETIC_PLAN_NAME = "fabric3.synthetic";
    private static final DeploymentPlan SYNTHETIC_PLAN = new DeploymentPlan(SYNTHETIC_PLAN_NAME);

    protected Deployer deployer;
    protected Generator generator;
    protected PolicyRegistry policyRegistry;
    protected boolean generateFullDeployment;

    protected List<DeployListener> listeners;

    protected MetaDataStore metadataStore;
    protected LogicalComponentManager logicalComponentManager;
    protected LogicalModelInstantiator logicalModelInstantiator;
    protected PolicyAttacher policyAttacher;
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
     * @param policyAttacher     the attacher for applying external attachment policies
     * @param deployer           the service for sending deployment commands
     * @param collector          the collector for undeploying components
     * @param contributionHelper the contribution helper
     * @param info               the host info
     */
    public AbstractDomain(MetaDataStore metadataStore,
                          LogicalComponentManager lcm,
                          Generator generator,
                          LogicalModelInstantiator instantiator,
                          PolicyAttacher policyAttacher,
                          Deployer deployer,
                          Collector collector,
                          ContributionHelper contributionHelper,
                          HostInfo info) {
        this.metadataStore = metadataStore;
        this.generator = generator;
        this.logicalModelInstantiator = instantiator;
        this.logicalComponentManager = lcm;
        this.policyAttacher = policyAttacher;
        this.deployer = deployer;
        this.collector = collector;
        this.contributionHelper = contributionHelper;
        this.info = info;
        listeners = Collections.emptyList();
    }

    public synchronized void include(QName deployable) throws DeploymentException {
        include(deployable, null);
    }

    public synchronized void include(QName deployable, String planName) throws DeploymentException {
        Composite wrapper = createWrapper(deployable);
        DeploymentPlan plan;
        if (planName == null) {
            if (RuntimeMode.CONTROLLER == info.getRuntimeMode() && !isLocal()) {
                plan = contributionHelper.findDefaultPlan(deployable);
                if (plan == null) {
                    plan = SYNTHETIC_PLAN;
                }
            } else {
                plan = SYNTHETIC_PLAN;
            }
        } else {
            // plan specified
            plan = contributionHelper.findPlan(planName);
            if (plan == null) {
                throw new DeploymentPlanNotFoundException("Deployment plan not found: " + planName);
            }
        }
        for (DeployListener listener : listeners) {
            listener.onDeploy(deployable, plan.getName());
        }
        instantiateAndDeploy(wrapper, plan, false);
        for (DeployListener listener : listeners) {
            listener.onDeployCompleted(deployable, plan.getName());
        }
    }

    public synchronized void include(Composite composite, boolean simulated) throws DeploymentException {
        QName name = composite.getName();
        for (DeployListener listener : listeners) {
            listener.onDeploy(name, SYNTHETIC_PLAN_NAME);
        }
        instantiateAndDeploy(composite, SYNTHETIC_PLAN, simulated);
    }

    public synchronized void include(List<URI> uris) throws DeploymentException {
        Set<Contribution> contributions = contributionHelper.findContributions(uris);
        List<Composite> deployables = contributionHelper.getDeployables(contributions);
        if (RuntimeMode.CONTROLLER == info.getRuntimeMode() && !isLocal()) {
            Map<URI, DeploymentPlan> plans = new HashMap<URI, DeploymentPlan>();
            for (Contribution contribution : contributions) {
                URI uri = contribution.getUri();
                DeploymentPlan defaultPlan = contributionHelper.findDefaultPlan(contribution);
                if (defaultPlan == null) {
                    defaultPlan = SYNTHETIC_PLAN;
                }
                plans.put(uri, defaultPlan);
            }
            DeploymentPlan merged = merge(plans.values());
            // notify listeners
            for (URI uri : uris) {
                for (DeployListener listener : listeners) {
                    listener.onDeploy(uri);
                }
            }
            for (Composite deployable : deployables) {
                QName name = deployable.getName();
                for (DeployListener listener : listeners) {
                    URI uri = deployable.getContributionUri();
                    DeploymentPlan plan = plans.get(uri);
                    listener.onDeploy(name, plan.getName());
                }
            }
            instantiateAndDeploy(deployables, contributions, merged, false);
            for (Composite deployable : deployables) {
                QName name = deployable.getName();
                for (DeployListener listener : listeners) {
                    URI uri = deployable.getContributionUri();
                    DeploymentPlan plan = plans.get(uri);
                    listener.onDeployCompleted(name, plan.getName());
                }
            }
            for (URI uri : uris) {
                for (DeployListener listener : listeners) {
                    listener.onDeployCompleted(uri);
                }
            }
        } else {
            // notify listeners
            for (URI uri : uris) {
                for (DeployListener listener : listeners) {
                    listener.onDeploy(uri);
                }
            }
            for (Composite deployable : deployables) {
                for (DeployListener listener : listeners) {
                    listener.onDeploy(deployable.getName(), SYNTHETIC_PLAN_NAME);
                }
            }
            instantiateAndDeploy(deployables, contributions, SYNTHETIC_PLAN, false);
            for (Composite deployable : deployables) {
                for (DeployListener listener : listeners) {
                    listener.onDeployCompleted(deployable.getName(), SYNTHETIC_PLAN_NAME);
                }
            }
            for (URI uri : uris) {
                for (DeployListener listener : listeners) {
                    listener.onDeployCompleted(uri);
                }
            }
        }
    }

    public synchronized void undeploy(URI uri, boolean force) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        if (deployables.isEmpty()) {
            return;
        }
        // reverse the deployables
        List<QName> names = new ArrayList<QName>();
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
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        for (QName deployable : names) {
            collector.markForCollection(deployable, domain);
        }
        try {
            Deployment deployment = generator.generate(domain, true);
            collector.collect(domain);
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false);
            }
            DeploymentPackage deploymentPackage = new DeploymentPackage(deployment, fullDeployment);
            try {
                deployer.deploy(deploymentPackage);
            } catch (DeploymentException e) {
                if (!force) {
                    throw e;
                }
                // force undeployment in effect: ignore deployment exceptions
            }
        } catch (GenerationException e) {
            StringBuffer list = new StringBuffer();
            for (QName deployable : names) {
                list.append(" ").append(deployable);
            }
            throw new DeploymentException("Error undeploying:" + list, e);
        }
        for (int i = 0, deployablesSize = names.size(); i < deployablesSize; i++) {
            QName deployable = names.get(i);
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

    public synchronized void undeploy(Composite composite, boolean simulated) throws DeploymentException {
        QName deployable = composite.getName();
        for (DeployListener listener : listeners) {
            listener.onUndeploy(deployable);
        }

        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        collector.markForCollection(deployable, domain);
        try {
            if (!simulated) {
                Deployment deployment = generator.generate(domain, true);
                collector.collect(domain);
                Deployment fullDeployment = null;
                if (generateFullDeployment) {
                    fullDeployment = generator.generate(domain, false);
                }
                DeploymentPackage deploymentPackage = new DeploymentPackage(deployment, fullDeployment);
                deployer.deploy(deploymentPackage);
            } else {
                collector.collect(domain);
            }
            URI uri = composite.getContributionUri();
            Contribution contribution = metadataStore.find(uri);
            contribution.releaseLock(deployable);
        } catch (GenerationException e) {
            throw new DeploymentException("Error undeploying:" + deployable);
        }
        logicalComponentManager.replaceRootComponent(domain);
        for (DeployListener listener : listeners) {
            listener.onUndeployCompleted(deployable);
        }
    }

    public synchronized void activateDefinitions(URI uri) throws DeploymentException {
        activateAndDeployDefinitions(uri, false);
    }

    public synchronized void deactivateDefinitions(URI uri) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + uri);
        }
        try {
            Set<PolicySet> policySets = policyRegistry.deactivateDefinitions(uri);
            if (!policySets.isEmpty()) {
                undeployPolicySets(policySets);
            }
        } catch (PolicyActivationException e) {
            throw new DeploymentException(e);
        }
    }

    public void recover(DomainJournal journal) throws DeploymentException {
        for (URI uri : journal.getContributions()) {
            activateAndDeployDefinitions(uri, true);
        }

        Map<QName, String> deployables = journal.getDeployables();
        Set<Contribution> contributions = new LinkedHashSet<Contribution>();
        List<DeploymentPlan> plans = new ArrayList<DeploymentPlan>();
        for (Map.Entry<QName, String> entry : deployables.entrySet()) {
            QName deployable = entry.getKey();
            String planName = entry.getValue();
            QNameSymbol symbol = new QNameSymbol(deployable);
            ResourceElement<QNameSymbol, Composite> element = metadataStore.find(Composite.class, symbol);
            if (element == null) {
                throw new DeploymentException("Contribution containing the deployable not found: " + deployable
                                                      + ". The domain journal (domain.xml) may be out of sync.");
            }
            Contribution contribution = element.getResource().getContribution();
            if (contribution == null) {
                // this should not happen
                throw new DeploymentException("Contribution for deployable not found: " + deployable);
            }
            contributions.add(contribution);
            DeploymentPlan plan;
            if (SYNTHETIC_PLAN_NAME.equals(planName)) {
                if (RuntimeMode.CONTROLLER == info.getRuntimeMode()) {
                    // this can happen if the composite is deployed in single VM mode and the runtime is later booted in controller mode
                    plan = contributionHelper.findDefaultPlan(deployable);
                    if (plan == null) {
                        plan = SYNTHETIC_PLAN;
                    }
                } else {
                    plan = SYNTHETIC_PLAN;
                }
            } else {
                plan = contributionHelper.findPlan(planName);
                if (plan == null) {
                    plan = SYNTHETIC_PLAN;
                }
            }
            if (plan == null) {
                // this should not happen
                throw new DeploymentPlanNotFoundException("Deployment plan not found: " + planName);
            }
            plans.add(plan);
        }
        DeploymentPlan merged = merge(plans);
        List<Composite> deployableComposites = contributionHelper.getDeployables(contributions);
        instantiateAndDeploy(deployableComposites, contributions, merged, true);
        // do not notify listeners
    }


    /**
     * Returns true if the domain is contained in a single VM.
     *
     * @return true if the domain is contained in a single VM
     */
    protected abstract boolean isLocal();

    /**
     * Returns true if the domain is enabled for transactional deployment.
     *
     * @return true if the domain is enabled for transactional deployment
     */
    protected abstract boolean isTransactional();

    /**
     * Selects bindings for references targeted to remote services for a set of components being deployed by delegating to a BindingSelector.
     *
     * @param domain the domain component
     * @throws DeploymentException if an error occurs during binding selection
     */
    protected void selectBinding(LogicalCompositeComponent domain) throws DeploymentException {
        // no-op
    }

    /**
     * Creates a wrapper used to include a composite at the domain level. The wrapper is thrown away during the inclusion.
     *
     * @param deployable the deployable being included
     * @return the composite wrapper
     * @throws DeploymentException if there is an error creating the composite wrapper
     */
    private Composite createWrapper(QName deployable) throws DeploymentException {
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

    private DeploymentPlan merge(Collection<DeploymentPlan> plans) {
        DeploymentPlan merged = new DeploymentPlan(SYNTHETIC_PLAN_NAME);
        for (DeploymentPlan plan : plans) {
            for (Map.Entry<QName, String> entry : plan.getDeployableMappings().entrySet()) {
                merged.addDeployableMapping(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    /**
     * Instantiates and optionally deploys deployables from a set of contributions. Deployment is performed if recovery mode is false or the runtime
     * is operating in single VM mode. When recovering in a distributed domain, the components contained in the deployables will be instantiated but
     * not deployed to zones. This is because the domain can run headless (i.e. without a controller) and may already be hosting deployed components.
     *
     * @param deployables   the deployables
     * @param contributions the contributions to deploy
     * @param plan          the deployment plan
     * @param recover       true if recovery mode is enabled
     * @throws DeploymentException if an error occurs during instantiation or deployment
     */
    private void instantiateAndDeploy(List<Composite> deployables, Set<Contribution> contributions, DeploymentPlan plan, boolean recover)
            throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        for (Contribution contribution : contributions) {
            if (ContributionState.INSTALLED != contribution.getState()) {
                throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
            }
        }

        // lock the contributions
        contributionHelper.lock(contributions);
        try {
            if (isTransactional()) {
                domain = CopyUtil.copy(domain);
            }
            for (Contribution contribution : contributions) {
                activateDefinitions(contribution);
            }
            InstantiationContext context = logicalModelInstantiator.include(deployables, domain);
            if (context.hasErrors()) {
                contributionHelper.releaseLocks(contributions);
                throw new AssemblyException(context.getErrors());
            }
            policyAttacher.attachPolicies(domain, !recover);
            if (!recover || RuntimeMode.VM == info.getRuntimeMode()) {
                // in single VM mode, recovery includes deployment
                allocateAndDeploy(domain, plan);
            } else {
                allocate(domain, plan);
                // Select bindings
                selectBinding(domain);
                collector.markAsProvisioned(domain);
            }

            logicalComponentManager.replaceRootComponent(domain);

        } catch (DeploymentException e) {
            // release the contribution locks if there was an error
            contributionHelper.releaseLocks(contributions);
            throw e;
        } catch (AllocationException e) {
            // release the contribution locks if there was an error
            contributionHelper.releaseLocks(contributions);
            throw new DeploymentException("Error deploying composite", e);
        } catch (PolicyResolutionException e) {
            // release the contribution locks if there was an error
            contributionHelper.releaseLocks(contributions);
            throw new DeploymentException("Error deploying composite", e);
        }
    }

    /**
     * Instantiates and deploys the given composite.
     *
     * @param composite the composite to instantiate and deploy
     * @param plan      the deployment plan to use or null
     * @param simulated true if the deployment is simulated
     * @throws DeploymentException if a deployment error occurs
     */
    private void instantiateAndDeploy(Composite composite, DeploymentPlan plan, boolean simulated) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        QName name = composite.getName();
        QNameSymbol symbol = new QNameSymbol(name);
        Contribution contribution = metadataStore.find(Composite.class, symbol).getResource().getContribution();
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
        }

        // check if the deployable has already been deployed by querying the lock owners
        if (contribution.getLockOwners().contains(name)) {
            throw new CompositeAlreadyDeployedException("Composite has already been deployed: " + name);
        }
        // lock the contribution
        contribution.acquireLock(name);
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        activateDefinitions(contribution);
        InstantiationContext context = logicalModelInstantiator.include(composite, domain);
        if (context.hasErrors()) {
            if (!simulated) {
                contribution.releaseLock(name);
                throw new AssemblyException(context.getErrors());
            }
        }
        try {
            policyAttacher.attachPolicies(domain, true);
        } catch (PolicyResolutionException e) {
            // release the contribution lock if there was an error
            if (contribution.getLockOwners().contains(name)) {
                contribution.releaseLock(name);
            }
            throw new DeploymentException(e);
        }
        if (!simulated) {
            try {
                allocateAndDeploy(domain, plan);
            } catch (DeploymentException e) {
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
     * @param plan   the deployment plan
     * @throws DeploymentException if an error is encountered during deployment
     */
    private void allocateAndDeploy(LogicalCompositeComponent domain, DeploymentPlan plan) throws DeploymentException {
        // Allocate the components to runtime nodes
        try {
            allocate(domain, plan);
        } catch (AllocationException e) {
            throw new DeploymentException("Error deploying composite", e);
        }
        // Select bindings
        selectBinding(domain);
        try {
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(domain, true);
            collector.markAsProvisioned(domain);
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false);
            }
            DeploymentPackage deploymentPackage = new DeploymentPackage(deployment, fullDeployment);
            deployer.deploy(deploymentPackage);
        } catch (GenerationException e) {
            throw new DeploymentException("Error deploying components", e);
        }
    }

    /**
     * Delegates to the Allocator to determine which runtimes to deploy the given collection of components to.
     *
     * @param domain the domain component
     * @param plan   the deployment plan
     * @throws AllocationException if an allocation error occurs
     */
    private void allocate(LogicalCompositeComponent domain, DeploymentPlan plan) throws AllocationException {
        if (allocator == null) {
            // allocator is an optional extension
            return;
        }
        for (LogicalResource<?> resource : domain.getResources()) {
            if (resource.getState() == LogicalState.NEW) {
                allocator.allocate(resource, plan);
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            if (channel.getState() == LogicalState.NEW) {
                allocator.allocate(channel, plan);
            }
        }
        Collection<LogicalComponent<?>> components = domain.getComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                allocator.allocate(component, plan);
            }
        }
    }

    /**
     * Activates and optionally deploys definitions to a domain.
     *
     * @param uri     the URI of the contribution containing the definitions to activate
     * @param recover true if recovery is being performed. If true and the runtime is in distributed (controller) mode, definitions will only be
     *                activated.
     * @throws DeploymentException if there is an error activating definitions
     */
    private synchronized void activateAndDeployDefinitions(URI uri, boolean recover) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (contribution == null) {
            throw new DeploymentException("Contribution not installed: " + uri);
        }
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + uri);
        }
        Set<PolicySet> policySets = activateDefinitions(contribution);
        if (!policySets.isEmpty()) {
            if (!recover || RuntimeMode.VM == info.getRuntimeMode()) {
                deployPolicySets(policySets);
            }
        }
    }


    /**
     * Activates policy definitions contained in the contribution.
     *
     * @param contribution the contribution
     * @return the policy sets activated
     * @throws DeploymentException if an exception occurs when the definitions are activated
     */
    private Set<PolicySet> activateDefinitions(Contribution contribution) throws DeploymentException {
        if (policyRegistry == null) {
            // registry not available until after bootstrap
            return Collections.emptySet();
        }
        try {
            return policyRegistry.activateDefinitions(contribution.getUri());
        } catch (PolicyActivationException e) {
            // TODO rollback policy activation
            throw new DeploymentException(e);
        }
    }

    private void deployPolicySets(Set<PolicySet> policySets) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        try {
            policyAttacher.attachPolicies(policySets, domain, true);
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(domain, true);
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false);
            }
            DeploymentPackage deploymentPackage = new DeploymentPackage(deployment, fullDeployment);
            deployer.deploy(deploymentPackage);
            logicalComponentManager.replaceRootComponent(domain);
        } catch (PolicyResolutionException e) {
            throw new DeploymentException(e);
        } catch (GenerationException e) {
            throw new DeploymentException(e);
        }
    }


    private void undeployPolicySets(Set<PolicySet> policySets) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        try {
            policyAttacher.detachPolicies(policySets, domain);
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(domain, true);
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false);
            }
            DeploymentPackage deploymentPackage = new DeploymentPackage(deployment, fullDeployment);
            deployer.deploy(deploymentPackage);
            logicalComponentManager.replaceRootComponent(domain);
        } catch (PolicyResolutionException e) {
            throw new DeploymentException(e);
        } catch (GenerationException e) {
            throw new DeploymentException(e);
        }
    }


}
