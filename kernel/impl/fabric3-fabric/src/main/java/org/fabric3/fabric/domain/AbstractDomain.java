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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.fabric3.host.domain.UndeploymentException;
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
import org.fabric3.spi.domain.DomainListener;
import org.fabric3.spi.domain.RoutingException;
import org.fabric3.spi.domain.RoutingService;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.CopyUtil;
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

    protected RoutingService routingService;
    protected Generator generator;
    // The service for allocating to remote zones. Domain subtypes may optionally inject this service if they support distributed domains.
    protected Allocator allocator;
    protected PolicyRegistry policyRegistry;

    protected List<DomainListener> listeners;

    private MetaDataStore metadataStore;
    private LogicalComponentManager logicalComponentManager;
    private LogicalModelInstantiator logicalModelInstantiator;
    private PolicyAttacher policyAttacher;
    private BindingSelector bindingSelector;
    private Collector collector;
    private ContributionHelper contributionHelper;
    private HostInfo info;

    /**
     * Constructor.
     *
     * @param metadataStore            the store for resolving contribution artifacts
     * @param logicalComponentManager  the manager for logical components
     * @param generator                the physical model generator
     * @param logicalModelInstantiator the logical model instantiator
     * @param policyAttacher           the attacher for applying external attachment policies
     * @param bindingSelector          the selector for binding.sca
     * @param routingService           the service for routing deployment commands
     * @param collector                the collector for undeploying components
     * @param contributionHelper       the contribution helper
     * @param info                     the host info
     */
    public AbstractDomain(MetaDataStore metadataStore,
                          LogicalComponentManager logicalComponentManager,
                          Generator generator,
                          LogicalModelInstantiator logicalModelInstantiator,
                          PolicyAttacher policyAttacher,
                          BindingSelector bindingSelector,
                          RoutingService routingService,
                          Collector collector,
                          ContributionHelper contributionHelper,
                          HostInfo info) {
        this.metadataStore = metadataStore;
        this.generator = generator;
        this.logicalModelInstantiator = logicalModelInstantiator;
        this.logicalComponentManager = logicalComponentManager;
        this.policyAttacher = policyAttacher;
        this.bindingSelector = bindingSelector;
        this.routingService = routingService;
        this.collector = collector;
        this.contributionHelper = contributionHelper;
        this.info = info;
        listeners = Collections.emptyList();
    }

    public synchronized void include(QName deployable) throws DeploymentException {
        include(deployable, null, false);
    }

    public synchronized void include(QName deployable, boolean transactional) throws DeploymentException {
        include(deployable, null, transactional);
    }

    public synchronized void include(QName deployable, String plan) throws DeploymentException {
        include(deployable, plan, false);
    }

    public synchronized void include(QName deployable, String plan, boolean transactional) throws DeploymentException {
        Composite composite = contributionHelper.resolveComposite(deployable);
        // In order to include a composite at the domain level, it must first be wrapped in a composite that includes it.
        // This wrapper is thrown away during the inclusion.
        Composite wrapper = new Composite(deployable);
        Include include = new Include();
        include.setName(deployable);
        include.setIncluded(composite);
        wrapper.add(include);
        if (plan == null) {
            DeploymentPlan deploymentPlan = null;
            if (RuntimeMode.CONTROLLER == info.getRuntimeMode()) {
                // default to first found deployment plan in a contribution if none specifed for a distributed deployment
                Contribution contribution = metadataStore.resolveContainingContribution(new QNameSymbol(deployable));
                for (Resource resource : contribution.getResources()) {
                    for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                        if (element.getValue() instanceof DeploymentPlan) {
                            deploymentPlan = (DeploymentPlan) element.getValue();
                            break;
                        }
                    }
                }
            }
            include(wrapper, deploymentPlan, transactional);
        } else {
            DeploymentPlan deploymentPlan = contributionHelper.resolvePlan(plan);
            include(wrapper, deploymentPlan, transactional);
        }
        for (DomainListener listener : listeners) {
            listener.onInclude(deployable, plan);
        }

    }

    public synchronized void include(Composite composite) throws DeploymentException {
        include(composite, null, false);
        for (DomainListener listener : listeners) {
            listener.onInclude(composite.getName(), null);
        }
    }

    public synchronized void include(List<URI> uris, boolean transactional) throws DeploymentException {
        Set<Contribution> contributions = contributionHelper.resolveContributions(uris);
        instantiateAndDeploy(contributions, null, false, transactional);
    }

    public synchronized void undeploy(QName deployable) throws UndeploymentException {
        undeploy(deployable, false);
    }

    public synchronized void undeploy(QName deployable, boolean transactional) throws UndeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        if (transactional) {
            domain = CopyUtil.copy(domain);
        }
        collector.markForCollection(deployable, domain);
        try {
            Deployment deployment = generator.generate(domain.getComponents(), true);
            routingService.route(deployment);
        } catch (GenerationException e) {
            throw new UndeploymentException("Error undeploying: " + deployable, e);
        } catch (RoutingException e) {
            throw new UndeploymentException("Error undeploying: " + deployable, e);

        }
        // TODO this should happen after nodes have undeployed the components and wires
        collector.collect(domain);
        logicalComponentManager.replaceRootComponent(domain);
        QNameSymbol deployableSymbol = new QNameSymbol(deployable);
        Contribution contribution = metadataStore.resolveContainingContribution(deployableSymbol);
        contribution.releaseLock(deployable);
        for (DomainListener listener : listeners) {
            listener.onUndeploy(deployable);
        }
    }

    public synchronized void activateDefinitions(URI uri, boolean apply, boolean transactional) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (contribution == null || ContributionState.INSTALLED != contribution.getState()) {
            // a composite may not be associated with a contribution, e.g. a bootstrap composite
            throw new ContributionNotInstalledException("Contribution is not installed: " + uri);
        }
        Set<AbstractPolicyDefinition> definitions = activateDefinitions(contribution);

        if (apply) {
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
                deployPolicySets(policySets, transactional);
            }
        }
    }

    public synchronized void deactivateDefinitions(URI uri, boolean transactional) throws DeploymentException {
        Contribution contribution = metadataStore.find(uri);
        if (contribution == null || ContributionState.INSTALLED != contribution.getState()) {
            // a composite may not be associated with a contribution, e.g. a bootstrap composite
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
            undeployPolicySet(policySets, transactional);
        }

    }

    private void deployPolicySets(List<PolicySet> policySets, boolean transactional) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (transactional) {
            domain = CopyUtil.copy(domain);
        }

        try {
            policyAttacher.attachPolicies(policySets, domain, true);
            Collection<LogicalComponent<?>> components = domain.getComponents();
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(components, true);
            routingService.route(deployment);

            logicalComponentManager.replaceRootComponent(domain);
        } catch (PolicyResolutionException e) {
            throw new DeploymentException(e);
        } catch (GenerationException e) {
            throw new DeploymentException(e);
        } catch (RoutingException e) {
            throw new DeploymentException(e);
        }
    }


    private void undeployPolicySet(List<PolicySet> policySets, boolean transactional) throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();
        if (transactional) {
            domain = CopyUtil.copy(domain);
        }

        try {
            policyAttacher.detachPolicies(policySets, domain);
            Collection<LogicalComponent<?>> components = domain.getComponents();
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(components, true);
            routingService.route(deployment);

            logicalComponentManager.replaceRootComponent(domain);
        } catch (PolicyResolutionException e) {
            throw new DeploymentException(e);
        } catch (GenerationException e) {
            throw new DeploymentException(e);
        } catch (RoutingException e) {
            throw new DeploymentException(e);
        }
    }

    public void recover(List<QName> deployables, List<String> planNames) throws DeploymentException {
        Set<Contribution> contributions = new LinkedHashSet<Contribution>();
        for (QName deployable : deployables) {
            QNameSymbol symbol = new QNameSymbol(deployable);
            Contribution contribution = metadataStore.resolveContainingContribution(symbol);
            if (contribution == null) {
                // this should not happen
                throw new DeploymentException("Contribution for deployable not found: " + deployable);
            }
            contributions.add(contribution);
        }
        instantiateAndDeploy(contributions, planNames, true, false);
    }

    public void recover(List<URI> uris) throws DeploymentException {
        Set<Contribution> contributions = contributionHelper.resolveContributions(uris);
        instantiateAndDeploy(contributions, null, true, false);
    }

    /**
     * Instantiates and optionally deploys all deployables from a set of contributions. Deployment is performed if recovery mode is false or the
     * runtme is operating in single VM mode. When recovering in a distributed domain, the components contained in the deployables will be
     * instantiated but not deployed to zones. This is because the domain can run headless (i.e. without a controller) and may already be hosting
     * deployed components. In the case where a recovery is performed for the entire domain, including zones, the controller will instantiate
     * components and zone managers will send synchronization requests to it, which will result in component deployments.
     *
     * @param contributions the contributions to deploy
     * @param planNames     the deployment plan names or null if no deployment plans are specified. If running in a distributed domain and no plans
     *                      are specified, the contributions will be introspected for deployment plans.
     * @param recover       true if recovery mode is enabled
     * @param transactional true if the deployment should be performed transactionally
     * @throws DeploymentException if an error occurs during instantiation or deployment
     */
    private void instantiateAndDeploy(Set<Contribution> contributions, List<String> planNames, boolean recover, boolean transactional)
            throws DeploymentException {
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        for (Contribution contribution : contributions) {
            if (ContributionState.INSTALLED != contribution.getState()) {
                throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
            }
        }

        List<Composite> deployables = contributionHelper.getDeployables(contributions, info.getRuntimeMode());

        List<DeploymentPlan> plans;
        if (planNames == null) {
            plans = contributionHelper.getDeploymentPlans(contributions);
        } else {
            plans = new ArrayList<DeploymentPlan>();
            for (String planName : planNames) {
                if (planName == null) {
                    plans.add(null);
                    continue;
                }
                DeploymentPlan plan = contributionHelper.resolvePlan(planName);
                if (plan == null) {
                    // this should not happen
                    throw new DeploymentException("Deployment plan not found: " + planName);
                }
                plans.add(plan);
            }
        }
        // lock the contributions
        contributionHelper.lock(contributions);
        try {
            if (transactional) {
                domain = CopyUtil.copy(domain);
            }
            for (Contribution contribution : contributions) {
                activateDefinitions(contribution);
            }
            InstantiationContext context = logicalModelInstantiator.include(deployables, domain);
            if (context.hasErrors()) {
                throw new AssemblyException(context.getErrors());
            }
            policyAttacher.attachPolicies(domain, !recover);
            if (!recover || RuntimeMode.VM == info.getRuntimeMode()) {
                // in single VM mode, recovery includes deployment
                allocateAndDeploy(domain, plans);
            } else {
                Collection<LogicalComponent<?>> components = domain.getComponents();
                allocate(components, plans);
                // Select bindings
                selectBinding(components);
                collector.markAsProvisioned(domain);
                logicalComponentManager.replaceRootComponent(domain);
            }

            // notify listeners
            for (int i = 0; i < deployables.size(); i++) {
                Composite deployable = deployables.get(i);
                String planName = null;
                if (!plans.isEmpty()) {
                    // deployment plans are not used in single-VM runtimes
                    // if only one plan is present, use it for every deployable
                    if (plans.size() == 1) {
                        planName = plans.get(0).getName();
                    } else {
                        planName = plans.get(i).getName();
                    }
                }
                for (DomainListener listener : listeners) {
                    listener.onInclude(deployable.getName(), planName);
                }
            }
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

    /**
     * Includes a composite in the domain composite.
     *
     * @param composite     the composite to include
     * @param plan          the deployment plan to use or null
     * @param transactional if the inclusion should be performed transactionally
     * @throws DeploymentException if a deployment error occurs
     */
    private void include(Composite composite, DeploymentPlan plan, boolean transactional) throws DeploymentException {
        List<DeploymentPlan> plans;
        if (plan != null) {
            plans = new ArrayList<DeploymentPlan>();
            plans.add(plan);
        } else {
            plans = Collections.emptyList();
        }
        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        QName name = composite.getName();
        Contribution contribution = metadataStore.resolveContainingContribution(new QNameSymbol(name));
        if (contribution != null && ContributionState.INSTALLED != contribution.getState()) {
            // a composite may not be associated with a contribution, e.g. a bootstrap composite
            throw new ContributionNotInstalledException("Contribution is not installed: " + contribution.getUri());
        }

        try {
            if (contribution != null) {
                // check if the deployable has already been deployed by querying the lock owners
                if (contribution.getLockOwners().contains(name)) {
                    throw new CompositeAlreadyDeployedException("Composite has already been deployed: " + name);
                }
                // lock the contribution
                contribution.acquireLock(name);
            }
            if (transactional) {
                domain = CopyUtil.copy(domain);
            }
            activateDefinitions(contribution);
            InstantiationContext context = logicalModelInstantiator.include(composite, domain);
            if (context.hasErrors()) {
                throw new AssemblyException(context.getErrors());
            }
            policyAttacher.attachPolicies(domain, true);
            allocateAndDeploy(domain, plans);
        } catch (DeploymentException e) {
            // release the contribution lock if there was an error
            if (contribution != null && contribution.getLockOwners().contains(name)) {
                contribution.releaseLock(name);
            }
            throw e;
        } catch (PolicyResolutionException e) {
            // release the contribution lock if there was an error
            if (contribution != null && contribution.getLockOwners().contains(name)) {
                contribution.releaseLock(name);
            }
            throw new DeploymentException(e);
        }
    }

    /**
     * Allocates and deploys new components in the domain.
     *
     * @param domain the domain component
     * @param plans  the deployment plans to use for deployment
     * @throws DeploymentException if an error is encountered during deployment
     */
    private void allocateAndDeploy(LogicalCompositeComponent domain, List<DeploymentPlan> plans) throws DeploymentException {
        Collection<LogicalComponent<?>> components = domain.getComponents();
        // Allocate the components to runtime nodes
        try {
            allocate(components, plans);
        } catch (AllocationException e) {
            throw new DeploymentException("Error deploying composite", e);
        }

        // Select bindings
        selectBinding(components);
        try {
            // generate and provision any new components and new wires
            Deployment deployment = generator.generate(components, true);
            routingService.route(deployment);
        } catch (GenerationException e) {
            throw new DeploymentException("Error deploying components", e);
        } catch (RoutingException e) {
            throw new DeploymentException("Error deploying components", e);
        }

        // TODO this should happen after nodes have deployed the components and wires
        collector.markAsProvisioned(domain);
        logicalComponentManager.replaceRootComponent(domain);
    }

    /**
     * Delegates to the Allocator to determine which runtimes to deploy the given collection of components to.
     *
     * @param components the components to allocate
     * @param plans      the deployment plans to use for allocation
     * @throws AllocationException if an allocation error occurs
     */
    private void allocate(Collection<LogicalComponent<?>> components, List<DeploymentPlan> plans) throws AllocationException {
        if (allocator == null) {
            // allocator is an optional extension
            return;
        }
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                allocator.allocate(component, plans, false);
            }
        }
    }

    /**
     * Selects bindings for references targeted to remote services for a set of components being deployed by delegating to a BindingSelector.
     *
     * @param components the set of components being deployed
     * @throws DeploymentException if an error occurs during binding selection
     */
    private void selectBinding(Collection<LogicalComponent<?>> components) throws DeploymentException {
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

}
