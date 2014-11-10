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
package org.fabric3.binding.ws.metro.generator.java.wsdl;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.BindingID;

/**
 * Generates WSDL documents from an SEI or implementation class. The returned generated WSDL is used to attach policy expressions with the resulting
 * document passed to the Metro infrastructure when an endpoint is provisioned.
 */
public interface JavaWsdlGenerator {

    /**
     * Generates the WSDL.
     *
     * @param seiClass        the SEI/implementation class
     * @param serviceQName    the service qualified name
     * @param endpointAddress the endpoint address
     * @param bindingId       the SOAP version to use
     * @return a the generated WSDLs and XSDs
     * @throws WsdlGenerationException if an error occurs during generation
     */
    GeneratedArtifacts generate(Class<?> seiClass, QName serviceQName, String endpointAddress, BindingID bindingId) throws WsdlGenerationException;
}