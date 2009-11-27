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
package org.fabric3.spi.contract;

import org.fabric3.model.type.contract.ServiceContract;

/**
 * Dermines if two <code>ServiceContract</code>s or compatible for wiring. Specifically, tests whether the target contract can be converted to the
 * source contract type. Some interface languages, such as Java, allow for inheritance. In these cases, compatibility will include checking if a
 * widening conversion is possible from the target contract to source contract.
 * <p/>
 * This service delegates to {@link ContractMatcherExtension}s for particular mappings such as WSDL-to-Java.
 *
 * @version $Rev$ $Date$
 */
public interface ContractMatcher {

    /**
     * Determines if two <code>ServiceContract</code>s are compatible for wiring.
     *
     * @param source the source contract. This is the contract specified by a component reference.
     * @param target the ctarget contract. This is the contract specified by a service.
     * @return true if the contracts are compatible
     */
    boolean isAssignableFrom(ServiceContract source, ServiceContract target);
}