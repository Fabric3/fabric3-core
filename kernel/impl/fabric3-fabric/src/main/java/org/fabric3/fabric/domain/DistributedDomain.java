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

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.Generator;
import org.fabric3.spi.domain.generator.binding.BindingSelector;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implements a distributed domain containing user-defined services.
 */
public class DistributedDomain extends AbstractDomain implements Domain {
    private BindingSelector bindingSelector;

    public DistributedDomain(@Reference(name = "store") MetaDataStore metaDataStore,
                             @Reference(name = "logicalComponentManager") LogicalComponentManager logicalComponentManager,
                             @Reference Generator generator,
                             @Reference LogicalModelInstantiator logicalModelInstantiator,
                             @Reference BindingSelector bindingSelector,
                             @Reference Deployer deployer,
                             @Reference Collector collector,
                             @Reference ContributionHelper contributionHelper,
                             @Reference HostInfo info) {
        super(metaDataStore, logicalComponentManager, generator, logicalModelInstantiator, deployer, collector, contributionHelper, info);
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

    protected void selectBinding(LogicalCompositeComponent domain) throws ContainerException {
        bindingSelector.selectBindings(domain);
    }

}
