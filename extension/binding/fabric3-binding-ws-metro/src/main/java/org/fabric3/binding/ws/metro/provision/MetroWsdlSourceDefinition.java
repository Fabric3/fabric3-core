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
package org.fabric3.binding.ws.metro.provision;

import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;

/**
 * Wire source definition for services that use WSDL-based contracts.
 *
 * @version $Rev$ $Date$
 */
public class MetroWsdlSourceDefinition extends MetroSourceDefinition {
    private static final long serialVersionUID = -1905843346636208650L;

    /**
     * Constructor.
     *
     * @param endpointDefinition endpoint metadata
     * @param wsdl               the WSDL document as a string
     * @param intents            intents configured at the endpoint level that are provided natively by the Metro
     * @param handlers           optional binding handlers
     */
    public MetroWsdlSourceDefinition(ServiceEndpointDefinition endpointDefinition,
                                     String wsdl,
                                     List<QName> intents,
                                     List<PhysicalBindingHandlerDefinition> handlers) {
        super(endpointDefinition, wsdl, intents, handlers);
        physicalDataTypes.clear();
        physicalDataTypes.add(PhysicalDataTypes.DOM);
    }

}