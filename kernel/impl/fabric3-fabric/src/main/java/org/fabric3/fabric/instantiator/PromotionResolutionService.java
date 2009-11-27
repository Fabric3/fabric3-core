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
package org.fabric3.fabric.instantiator;

import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Resolves promoted services and references by setting the resolved promotion URI of the logical component service or reference that is being
 * promoted.
 *
 * @version $Rev$ $Date$
 */
public interface PromotionResolutionService {

    /**
     * Handles promotion on the specified logical service.
     * <p/>
     * Promoted URIs are of the general form <code>componentId#serviceName</code>, where the service name is optional. If the  promoted URI doesn't
     * contain a fragment for the service name, the promoted component is expected to have exactly one service. If the service fragment is present the
     * promoted component is required to have a service by the name. If the service fragment was not specified, the promoted URI is set to the URI of
     * the promoted service.
     *
     * @param logicalService Logical service whose promotion is handled.
     * @param context        the instantiation context. Recoverable errors and warnings should be reported here.
     */
    void resolve(LogicalService logicalService, InstantiationContext context);

    /**
     * Handles all promotions on the specified logical reference.
     * <p/>
     * Promoted URIs are of the general form <code>componentId#referenceName</code>, where the reference name is optional. If the  promoted URI
     * doesn't contain a fragment for the reference name, the promoted component is expected to have exactly one reference. If the reference fragment
     * is present the promoted component is required to have a reference by the name. If the reference fragment was not specified, the promoted URI is
     * set to the URI of the promoted reference.
     *
     * @param logicalReference Logical reference whose promotion is handled.
     * @param context          the instantiation context. Recoverable errors and warnings should be reported here.
     */
    void resolve(LogicalReference logicalReference, InstantiationContext context);

}
