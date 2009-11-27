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
package org.fabric3.jpa.spi.delegate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.fabric3.jpa.spi.EmfBuilderException;

/**
 * Delegate interface for creating entity manager factories for adding provider specific hook-ins.
 *
 * @version $Rev$ $Date$
 */
public interface EmfBuilderDelegate {

    /**
     * Builds the entity managed factory.
     *
     * @param info           Persistence unit info.
     * @param classLoader    Classloader to use.
     * @param dataSourceName the data soruce name.
     * @return Entity manager factory.
     * @throws EmfBuilderException if an error building the factory occurs
     */
    EntityManagerFactory build(PersistenceUnitInfo info, ClassLoader classLoader, String dataSourceName) throws EmfBuilderException;

}
