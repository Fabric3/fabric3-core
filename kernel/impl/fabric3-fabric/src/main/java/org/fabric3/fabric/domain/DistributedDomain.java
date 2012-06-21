/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.generator.policy.PolicyAttacher;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implements a distributed domain containing user-defined services.
 *
 * @version $Rev$ $Date$
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
        super(metaDataStore,
              logicalComponentManager,
              generator,
              logicalModelInstantiator,
              policyAttacher,
              deployer,
              collector,
              contributionHelper,
              info);
        generateFullDeployment = RuntimeMode.CONTROLLER == info.getRuntimeMode();
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
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    protected boolean isLocal() {
        // classloader isolation check needed for webapp runtime
        return info.supportsClassLoaderIsolation() && RuntimeMode.CONTROLLER != info.getRuntimeMode();
    }

    protected boolean isTransactional() {
        if (info.getRuntimeMode() == RuntimeMode.CONTROLLER) {
            return true;
        } else if (info.getRuntimeMode() == RuntimeMode.VM) {
            return transactional;
        }
        return false;
    }

    protected void selectBinding(LogicalCompositeComponent domain) throws DeploymentException {
        try {
            bindingSelector.selectBindings(domain);
        } catch (BindingSelectionException e) {
            throw new DeploymentException(e);
        }
    }



}
