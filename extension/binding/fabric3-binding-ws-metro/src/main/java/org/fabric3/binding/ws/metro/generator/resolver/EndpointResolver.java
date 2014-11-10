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
