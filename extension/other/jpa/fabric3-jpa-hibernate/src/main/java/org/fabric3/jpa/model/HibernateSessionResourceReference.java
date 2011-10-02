/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.jpa.model;

import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;

/**
 * Represents a Hibernate Session treated as a resource.
 *
 * @version $Rev: 7652 $ $Date: 2009-09-20 16:02:49 +0200 (Sun, 20 Sep 2009) $
 */
public final class HibernateSessionResourceReference extends ResourceReferenceDefinition {
    private static final long serialVersionUID = 4343784880360787751L;
    private String unitName;
    private boolean multiThreaded;

    /**
     * Constructor.
     *
     * @param name            Name of the resource.
     * @param unitName        Persistence unit name.
     * @param serviceContract the service contract for the persistence unit
     * @param multiThreaded   true if the resource is accessed from a multi-threaded implementation
     */
    public HibernateSessionResourceReference(String name, String unitName, ServiceContract serviceContract, boolean multiThreaded) {
        super(name, serviceContract, true);
        this.unitName = unitName;
        this.multiThreaded = multiThreaded;
    }

    /**
     * Returns the persistence unit name.
     *
     * @return the persistence unit name.
     */
    public final String getUnitName() {
        return this.unitName;
    }

    /**
     * Returns true if the EntityManager will be accessed from a mutli-thread implementation.
     *
     * @return true if the EntityManager will be accessed from a mutli-thread implementation
     */
    public boolean isMultiThreaded() {
        return multiThreaded;
    }

}