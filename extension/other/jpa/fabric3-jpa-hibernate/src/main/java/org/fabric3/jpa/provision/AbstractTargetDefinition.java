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
package org.fabric3.jpa.provision;

import java.util.Collections;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * Contains general attach point metadata for Hibernate and JPA resources.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractTargetDefinition extends PhysicalTargetDefinition {
    private static final long serialVersionUID = 2196282576920362492L;
    private String unitName;
    private PersistenceOverrides overrides;

    protected AbstractTargetDefinition(String unitName) {
        this.unitName = unitName;
        overrides = new PersistenceOverrides(unitName, Collections.<String, String>emptyMap());
    }

    /**
     * Returns the persistence unit name.
     *
     * @return the persistence unit name
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Returns property overrides for a collection of persistence contexts or Hibernate Sessions.
     *
     * @return property overrides for a collection of persistence contexts or Hibernate Sessions
     */
    public PersistenceOverrides getOverrides() {
        return overrides;
    }

    /**
     * Sets property overrides for a collection of the persistence contexts or Hibernate Sessions.
     *
     * @param overrides property overrides for a collection of persistence contexts or Hibernate Sessions
     */
    public void setOverrides(PersistenceOverrides overrides) {
        this.overrides = overrides;
    }


}