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
import java.net.URI;

import org.fabric3.binding.ws.metro.generator.WsdlElement;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;

/**
 * Resolves endpoint information contained in a WSDL document. This is done by parsing the WSDL element URI which must be of the form
 * <code><WSDL-namespace-URI>#expression<code>. The WSDL namespace URI is used to determine the set of documents the expression is applied against. If
 * a WSDL location is not provided, the set of documents will be constrained by the import/exports of the contribution containing the deployable
 * composite. In other words, WSDL documents not visible in the deployable composite's contribution space will not be evaluated.
 */
public interface EndpointResolver {

    /**
     * Resolves service-side endpoint information against a parsed WSDL document.
     *
     * @param wsdlElement the parsed WSDL element expression
     * @param wsdl        the parsed WSL
     * @return the service-side endpoint information
     * @throws EndpointResolutionException if an error performing resolution is encountered
     */
    ServiceEndpointDefinition resolveServiceEndpoint(WsdlElement wsdlElement, Definition wsdl) throws EndpointResolutionException;

    /**
     * Resolves service-side endpoint information against a parsed WSDL document, overriding the target URI specified in the WSDL.
     *
     * @param wsdlElement the parsed WSDL element expression
     * @param wsdl        the parsed WSL
     * @param uri         the URI to override the WSDL-specified URI with
     * @return the service-side endpoint information
     * @throws EndpointResolutionException if an error performing resolution is encountered
     */
    ServiceEndpointDefinition resolveServiceEndpoint(WsdlElement wsdlElement, Definition wsdl, URI uri) throws EndpointResolutionException;


    /**
     * Resolves reference-side endpoint information against a parsed WSDL document.
     *
     * @param wsdlElement the parsed WSDL element expression
     * @param wsdl        the parsed WSL
     * @return the reference-side endpoint information
     * @throws EndpointResolutionException if an error performing resolution is encountered
     */
    ReferenceEndpointDefinition resolveReferenceEndpoint(WsdlElement wsdlElement, Definition wsdl) throws EndpointResolutionException;


    /**
     * Serializes the contents of a parsed WSDL as a string.
     *
     * @param wsdl the WSDL
     * @return the serialized WSDL
     * @throws EndpointResolutionException if an error occurs reading the URL
     */
    public String serializeWsdl(Definition wsdl) throws EndpointResolutionException;

}
