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
package org.fabric3.spi.model.instance;

import java.net.URI;

import org.fabric3.model.type.component.ResourceDefinition;

/**
 * Represents a resource on an instantiated component in the domain.
 *
 * @version $Rev$ $Date$
 */
public class LogicalResource<RD extends ResourceDefinition> extends LogicalAttachPoint {
    private static final long serialVersionUID = -6298167441706672513L;

    private RD resourceDefinition;
    private URI target;

    /**
     * Initializes the URI and the resource definition.
     *
     * @param uri                URI of the resource.
     * @param resourceDefinition Definition of the resource.
     * @param parent             the parent component
     */
    public LogicalResource(URI uri, RD resourceDefinition, LogicalComponent<?> parent) {
        super(uri, resourceDefinition != null ? resourceDefinition.getServiceContract() : null, parent);
        this.resourceDefinition = resourceDefinition;
    }

    /**
     * Gets the definition for this resource.
     *
     * @return Definition for this resource.
     */
    public final RD getResourceDefinition() {
        return resourceDefinition;
    }

    /**
     * Gets the target for the resource.
     *
     * @return Resource target.
     */
    public URI getTarget() {
        return target;
    }

    /**
     * Sets the target for the resource.
     *
     * @param target Resource target.
     */
    public void setTarget(URI target) {
        this.target = target;
    }

}
