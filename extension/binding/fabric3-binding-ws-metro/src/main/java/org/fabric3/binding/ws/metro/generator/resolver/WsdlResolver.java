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
package org.fabric3.binding.ws.metro.generator.resolver;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;

/**
 * Resolves parsed WSDLs against an external location or those visible to a contribution installed in the domain.
 */
public interface WsdlResolver {

    /**
     * Resolve the WSDL against the external location.
     *
     * @param wsdlLocation the location of the WSDL documemt
     * @return the parsed WSDL
     * @throws WsdlResolutionException if a resolution error occurs
     */
    Definition parseWsdl(URL wsdlLocation) throws WsdlResolutionException;

    /**
     * Resolve the WSDL against the WSDLs installed in the domain for the given contribution.
     *
     * @param contributionUri the contribution URI
     * @param wsdlName        the WSDL name
     * @return the parsed WSDL
     * @throws WsdlResolutionException if a resolution error occurs
     */
    Definition resolveWsdl(URI contributionUri, QName wsdlName) throws WsdlResolutionException;

    /**
     * Resolve the WSDL against the WSDLs installed in the domain for the given contribution by port name.
     *
     * @param contributionUri the contribution URI
     * @param portName        the WSDL port name
     * @return the parsed WSDL
     * @throws WsdlResolutionException if a resolution error occurs
     */
    Definition resolveWsdlByPortName(URI contributionUri, QName portName) throws WsdlResolutionException;

    /**
     * Resolve the WSDL against the WSDLs installed in the domain for the given contribution by service name.
     *
     * @param contributionUri the contribution URI
     * @param serviceName     the WSDL service name
     * @return the parsed WSDL
     * @throws WsdlResolutionException if a resolution error occurs
     */
    Definition resolveWsdlByServiceName(URI contributionUri, QName serviceName) throws WsdlResolutionException;

    /**
     * Resolve the WSDL against the WSDLs installed in the domain for the given contribution by binding name.
     *
     * @param contributionUri the contribution URI
     * @param bindingName     the WSDL binding name
     * @return the parsed WSDL
     * @throws WsdlResolutionException if a resolution error occurs
     */
    Definition resolveWsdlByBindingName(URI contributionUri, QName bindingName) throws WsdlResolutionException;
}
