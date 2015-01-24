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

import java.util.List;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.Generator;
import org.fabric3.spi.domain.generator.binding.BindingSelectionException;
import org.fabric3.spi.domain.generator.binding.BindingSelector;
import org.fabric3.spi.domain.generator.policy.PolicyAttacher;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implements a distributed domain containing user-defined services.
 */
public class DistributedDomain extends AbstractDomain implements Domain {
    private boolean transactional;
    private BindingSelector bindingSelector;

    public DistributedDomain(@Reference(name = "store") MetaDataStore metaDataStore,
                             @Reference(name = "logicalComponentManager") LogicalComponentManager logicalComponentManager,
                             @Reference Generator generator,
                             @Reference LogicalModelInstantiator logicalModelInstantiator,
                             @Reference PolicyAttacher policyAttacher,
                             @Reference BindingSelector bindingSelector,
                             @Reference Deployer deployer,
                             @Reference Collector collector,
                             @Reference ContributionHelper contributionHelper,
                             @Reference HostInfo info) {
        super(metaDataStore, logicalComponentManager, generator, logicalModelInstantiator, policyAttacher, deployer, collector, contributionHelper, info);
        this.bindingSelector = bindingSelector;
    }

    /**
     * Used to optionally inject an Allocator.
     *
     * @param allocator the allocator
     */
    @Reference(required = false)
    public void setAllocator(Allocator allocator) {
        this.allocator = allocator;
    }

    /**
     * Used to optionally reinject a Deployer
     *
     * @param deployer the deployer
     */
    @Reference
    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    /**
     * Used to optionally inject DomainListeners.
     *
     * @param listeners the listeners
     */
    @Reference(required = false)
    public void setListeners(List<DeployListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * Used to inject the PolicyRegistry after bootstrap.
     *
     * @param policyRegistry the registry
     */
    @Reference(required = false)
    public void setPolicyRegistry(PolicyRegistry policyRegistry) {
        this.policyRegistry = policyRegistry;
    }

    /**
     * Optionally used to override default non-transactional deployment behavior in the single-VM runtime.
     *
     * @param transactional used to override default non-transactional deployment behavior in the single-VM runtime
     */
    @Property(required = false)
    @Source("$systemConfig//f3:deployment/@transactional")
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    protected boolean isTransactional() {
        return info.getRuntimeMode() == RuntimeMode.VM && transactional;
    }

    protected void selectBinding(LogicalCompositeComponent domain) throws DeploymentException {
        try {
            bindingSelector.selectBindings(domain);
        } catch (BindingSelectionException e) {
            throw new DeploymentException(e);
        }
    }

}
