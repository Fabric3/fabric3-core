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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.domain.DomainListener;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.CopyUtil;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.plan.DeploymentPlan;
import org.fabric3.spi.policy.PolicyActivationException;
import org.fabric3.spi.policy.PolicyAttacher;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.policy.PolicyResolutionException;

/**
 * Base class for a domain.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDomain implements Domain {
    private static final String SYNTHETIC_PLAN_NAME = "fabric3.synthetic";
    private static final DeploymentPlan SYNTHETIC_PLAN = new DeploymentPlan(SYNTHETIC_PLAN_NAME);

    protected Deployer deployer;
    protected Generator generator;
    protected PolicyRegistry policyRegistry;
    protected boolean generateFullDeployment;

    protected List<DomainListener> listeners;

    protected MetaDataStore metadataStore;
    protected LogicalComponentManager logicalComponentManager;
    protected LogicalModelInstantiator logicalModelInstantiator;
    protected PolicyAttacher policyAttacher;
    protected BindingSelector bindingSelector;
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
     * @param bindingSelector    the selector for binding.sca
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
                          BindingSelector bindingSelector,
                          Deployer deployer,
                          Collector collector,
                          ContributionHelper contributionHelper,
                          HostInfo info) {
        this.metadataStore = metadataStore;
        this.generator = generator;
        this.logicalModelInstantiator = instantiator;
        this.logicalComponentManager = lcm;
        this.policyAttacher = policyAttacher;
        this.bindingSelector = bindingSelector;
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
            // plan not specified
            plan = contributionHelper.findPlan(planName);
            if (plan == null) {
                throw new DeploymentPlanNotFoundException("Deployment plan not found: " + planName);
            }
        }
        instantiateAndDeploy(wrapper, plan);
        for (DomainListener listener : listeners) {
            listener.onInclude(deployable, plan.getName());
        }
    }

    public synchronized void include(Composite composite) throws DeploymentException {
        instantiateAndDeploy(composite, SYNTHETIC_PLAN);
        QName name = composite.getName();
        for (DomainListener listener : listeners) {
            listener.onInclude(name, SYNTHETIC_PLAN_NAME);
        }
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
            instantiateAndDeploy(deployables, contributions, merged, false);

            // notify listeners
            for (Composite deployable : deployables) {
                QName name = deployable.getName();
                for (DomainListener listener : listeners) {
                    URI uri = deployable.getContributionUri();
                    DeploymentPlan plan = plans.get(uri);
                    listener.onInclude(name, plan.getName());
                }
            }
        } else {
            instantiateAndDeploy(deployables, contributions, SYNTHETIC_PLAN, false);
            // notify listeners
            for (Composite deployable : deployables) {
                for (DomainListener listener : listeners) {
                    listener.onInclude(deployable.getName(), SYNTHETIC_PLAN_NAME);
                }
            }
        }
    }

    public synchronized void undeploy(QName deployable) throws DeploymentException {
        undeploy(deployable, false);
    }

    public void undeploy(QName deployable, boolean force) throws DeploymentException {
        QNameSymbol deployableSymbol = new QNameSymbol(deployable);
        Contribution contribution = metadataStore.find(DeploymentPlan.class, deployableSymbol).getResource().getContribution();
        if (!contribution.getLockOwners().contains(deployable)) {
            throw new CompositeNotDeployedException("Composite is not deployed: " + deployable);
        }
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        collector.markForCollection(deployable, domain);
        try {
            Deployment deployment = generator.generate(domain, true, isLocal());
            collector.collect(domain);
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false, isLocal());
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
            throw new DeploymentException("Error undeploying: " + deployable, e);
        }
        contribution.releaseLock(deployable);
        logicalComponentManager.replaceRootComponent(domain);
        for (DomainListener listener : listeners) {
            listener.onUndeploy(deployable);
        }
    }

    public synchronized void activateDefinitions(URI uri) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + uri);
        }
        Set<AbstractPolicyDefinition> definitions = activateDefinitions(contribution);
        List<PolicySet> policySets = new ArrayList<PolicySet>();
        for (AbstractPolicyDefinition definition : definitions) {
            if (definition instanceof PolicySet) {
                PolicySet policySet = (PolicySet) definition;
                if (policySet.getAttachTo() != null) {
                    policySets.add(policySet);
                }
            }
        }
        if (!policySets.isEmpty()) {
            deployPolicySets(policySets);
        }
    }

    public synchronized void deactivateDefinitions(URI uri) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + uri);
        }
        List<PolicySet> policySets = new ArrayList<PolicySet>();
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (!(element.getValue() instanceof AbstractPolicyDefinition)) {
                    break;
                }
                AbstractPolicyDefinition definition = (AbstractPolicyDefinition) element.getValue();
                try {
                    policyRegistry.deactivate(definition);
                    if (definition instanceof PolicySet) {
                        PolicySet policySet = (PolicySet) definition;
                        if (policySet.getAttachTo() != null) {
                            policySets.add(policySet);
                        }
                    }
                } catch (PolicyActivationException e) {
                    throw new DeploymentException(e);
                }
            }
        }
        if (!policySets.isEmpty()) {
            undeployPolicySets(policySets);
        }

    }

    public void recover(Map<QName, String> deployables) throws DeploymentException {
        Set<Contribution> contributions = new LinkedHashSet<Contribution>();
        List<DeploymentPlan> plans = new ArrayList<DeploymentPlan>();
        for (Map.Entry<QName, String> entry : deployables.entrySet()) {
            QName deployable = entry.getKey();
            String planName = entry.getValue();
            QNameSymbol symbol = new QNameSymbol(deployable);
            Contribution contribution = metadataStore.find(Composite.class, symbol).getResource().getContribution();
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
     * Instantiates and optionally deploys deployables from a set of contributions. Deployment is performed if recovery mode is false or the runtme is
     * operating in single VM mode. When recovering in a distributed domain, the components contained in the deployables will be instantiated but not
     * deployed to zones. This is because the domain can run headless (i.e. without a controller) and may already be hosting deployed components.
     *
     * @param deployables   the depoyables
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
     * @throws DeploymentException if a deployment error occurs
     */
    private void instantiateAndDeploy(Composite composite, DeploymentPlan plan) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        QName name = composite.getName();
        QNameSymbol symbol = new QNameSymbol(name);
        Contribution contribution = metadataStore.find(Composite.class, symbol).getResource().getContribution();
        if (ContributionState.INSTALLED != contribution.getState()) {
            throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
        }

        try {
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
                contribution.releaseLock(name);
                throw new AssemblyException(context.getErrors());
            }
            policyAttacher.attachPolicies(domain, true);
            allocateAndDeploy(domain, plan);
            logicalComponentManager.replaceRootComponent(domain);
        } catch (DeploymentException e) {
            // release the contribution lock if there was an error
            if (contribution.getLockOwners().contains(name)) {
                contribution.releaseLock(name);
            }
            throw e;
        } catch (PolicyResolutionException e) {
            // release the contribution lock if there was an error
            if (contribution.getLockOwners().contains(name)) {
                contribution.releaseLock(name);
            }
            throw new DeploymentException(e);
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
            Deployment deployment = generator.generate(domain, true, isLocal());
            collector.markAsProvisioned(domain);
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false, isLocal());
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
     * Selects bindings for references targeted to remote services for a set of components being deployed by delegating to a BindingSelector.
     *
     * @param domain the domain component
     * @throws DeploymentException if an error occurs during binding selection
     */
    private void selectBinding(LogicalCompositeComponent domain) throws DeploymentException {
        Collection<LogicalComponent<?>> components = domain.getComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                try {
                    bindingSelector.selectBindings(component);
                } catch (BindingSelectionException e) {
                    throw new DeploymentException(e);
                }
            }
        }
    }

    /**
     * Activates policy definitions contained in the contribution.
     *
     * @param contribution the contribution
     * @return the policy definitions activated
     * @throws DeploymentException if an exception occurs when the definitions are activated
     */
    private Set<AbstractPolicyDefinition> activateDefinitions(Contribution contribution) throws DeploymentException {
        if (policyRegistry == null) {
            // registry not available until after bootstrap
            return Collections.emptySet();
        }
        Set<AbstractPolicyDefinition> definitions = new HashSet<AbstractPolicyDefinition>();
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (!(element.getValue() instanceof AbstractPolicyDefinition)) {
                    break;
                }
                try {
                    AbstractPolicyDefinition definition = (AbstractPolicyDefinition) element.getValue();
                    definitions.add(definition);
                    policyRegistry.activate(definition);
                } catch (PolicyActivationException e) {
                    // TODO rollback policy activation
                    throw new DeploymentException(e);
                }
            }
        }
        return definitions;
    }

    private void deployPolicySets(List<PolicySet> policySets) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        try {
            policyAttacher.attachPolicies(policySets, domain, true);
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(domain, true, isLocal());
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false, isLocal());
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


    private void undeployPolicySets(List<PolicySet> policySets) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (isTransactional()) {
            domain = CopyUtil.copy(domain);
        }
        try {
            policyAttacher.detachPolicies(policySets, domain);
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(domain, true, isLocal());
            Deployment fullDeployment = null;
            if (generateFullDeployment) {
                fullDeployment = generator.generate(domain, false, isLocal());
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
