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

import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.DomainListener;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.policy.PolicyAttacher;
import org.fabric3.spi.policy.PolicyRegistry;

/**
 * Implements a distributed domain containing user-defined services.
 *
 * @version $Rev$ $Date$
 */
public class DistributedDomain extends AbstractDomain implements Domain {

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
        super(metaDataStore,
              logicalComponentManager,
              generator,
              logicalModelInstantiator,
              policyAttacher,
              bindingSelector,
              deployer,
              collector,
              contributionHelper,
              info);
        generateFullDeployment = RuntimeMode.CONTROLLER == info.getRuntimeMode();
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
    public void setListeners(List<DomainListener> listeners) {
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

    protected boolean isLocal() {
        // classloader isolation check needed for webapp runtime
        return info.supportsClassLoaderIsolation() && RuntimeMode.CONTROLLER != info.getRuntimeMode();
    }
}
