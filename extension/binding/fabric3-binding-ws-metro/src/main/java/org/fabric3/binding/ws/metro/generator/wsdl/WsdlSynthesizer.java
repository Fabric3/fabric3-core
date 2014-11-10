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
package org.fabric3.binding.ws.metro.generator.wsdl;

import java.net.URI;
import javax.wsdl.Definition;

import org.fabric3.api.binding.ws.model.WsBindingDefinition;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * Synthesizes a concrete WSDL (i.e. service, port and binding information) from a binding configuration and an abstract WSDL definition.
 */
public interface WsdlSynthesizer {

    /**
     * Generate a concrete WSDL using the given abstract WSDL and binding configuration.
     *
     * @param binding         the binding to synthesize the WSDL for
     * @param endpointAddress the service endpoint address
     * @param contract        the endpoint service contract
     * @param policy          configured policy for the service
     * @param wsdl            the abstract WSDL
     * @param targetUri       the endpoint URI
     * @return a result containing the concrete WSDL and the generated service and port names
     * @throws WsdlSynthesisException if an error occurs during synthesis
     */
    ConcreteWsdlResult synthesize(LogicalBinding<WsBindingDefinition> binding,
                                   String endpointAddress,
                                   WsdlServiceContract contract,
                                   EffectivePolicy policy,
                                   Definition wsdl,
                                   URI targetUri) throws WsdlSynthesisException;


}