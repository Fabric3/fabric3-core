/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
