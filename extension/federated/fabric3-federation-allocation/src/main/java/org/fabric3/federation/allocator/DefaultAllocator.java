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
package org.fabric3.federation.allocator;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.allocator.AllocationException;
import org.fabric3.spi.allocator.Allocator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Allocator that selectes zones for a collection of components using deployment plan mappings.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DefaultAllocator implements Allocator {

    public void allocate(LogicalComponent<?> component, DeploymentPlan plan) throws AllocationException {
        if (LogicalComponent.LOCAL_ZONE.equals(component.getZone())) {
            if (component instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
                for (LogicalComponent<?> child : composite.getComponents()) {
                    allocate(child, plan);
                }
                for (LogicalChannel channel : composite.getChannels()) {
                    selectZone(channel, plan);
                }
            }
            selectZone(component, plan);
        }
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

    private void selectZone(LogicalChannel channel, DeploymentPlan plan) throws AllocationException {
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

}
