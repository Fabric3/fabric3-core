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
package org.fabric3.federation.allocator;

import javax.xml.namespace.QName;

import org.fabric3.api.host.Names;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.domain.allocator.AllocationException;
import org.fabric3.spi.domain.allocator.Allocator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.plan.DeploymentPlan;

/**
 * Allocator that selects zones for a collection of components using deployment plan mappings. Used in controller runtimes.
 */
@EagerInit
public class ControllerAllocator implements Allocator {

    public void allocate(LogicalComponent<?> component, DeploymentPlan plan) throws AllocationException {
        if (Names.LOCAL_ZONE.equals(component.getZone())) {
            if (component instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
                for (LogicalComponent<?> child : composite.getComponents()) {
                    allocate(child, plan);
                }
                for (LogicalResource<?> resource : composite.getResources()) {
                    allocate(resource, plan);
                }
                for (LogicalChannel channel : composite.getChannels()) {
                    allocate(channel, plan);
                }
            }
            selectZone(component, plan);
        }
    }

    public void allocate(LogicalChannel channel, DeploymentPlan plan) throws AllocationException {
        QName deployable = channel.getDeployable();
        if (deployable == null) {
            // programming error
            throw new AssertionError("Deployable not found for " + channel.getUri());
        }
        String zoneName = plan.getDeployableMappings().get(deployable);
        if (zoneName == null) {
            throw new DeployableMappingNotFoundException("Zone mapping not found for deployable: " + deployable);
        }
        channel.setZone(zoneName);
    }

    public void allocate(LogicalResource<?> resource, DeploymentPlan plan) throws AllocationException {
        QName deployable = resource.getDeployable();
        if (deployable == null) {
            // programming error
            throw new AssertionError("Deployable not found for resource");
        }
        String zoneName = plan.getDeployableMappings().get(deployable);
        if (zoneName == null) {
            throw new DeployableMappingNotFoundException("Zone mapping not found for deployable: " + deployable);
        }
        resource.setZone(zoneName);
    }

    /**
     * Maps a component to a zone based on a collection of deployment plans.
     *
     * @param component the component to map
     * @param plan      the deployment plans to use for mapping
     * @throws AllocationException if an error occurs mapping
     */
    private void selectZone(LogicalComponent<?> component, DeploymentPlan plan) throws AllocationException {
        QName deployable = component.getDeployable();
        if (deployable == null) {
            // programming error
            throw new AssertionError("Deployable not found for " + component.getUri());
        }
        String zoneName = plan.getDeployableMappings().get(deployable);
        if (zoneName == null) {
            throw new DeployableMappingNotFoundException("Zone mapping not found for deployable: " + deployable);
        }
        component.setZone(zoneName);
    }

}
