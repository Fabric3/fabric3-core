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
package org.fabric3.fabric.instantiator.wire;

import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Resolves the service contract for services and references. Promoted services and references often do not specify a service contract explicitly,
 * instead using a contract defined further down in the promotion hierarchy. In these cases, the service contract is often inferred from the
 * implementation (e.g. a Java class) or explicitly declared within the component definition in a composite file.
 *
 * @version $Rev$ $Date$
 */
public interface ServiceContractResolver {

    /**
     * Returns the contract for a service.
     *
     * @param service the service to determine the service contract for.
     * @return the contract or null if none is found
     */
    ServiceContract determineContract(LogicalService service);

    /**
     * Returns the contract for a reference.
     *
     * @param reference the reference to determine the service contract for.
     * @return the contract or null if none is found
     */
    ServiceContract determineContract(LogicalReference reference);

}
