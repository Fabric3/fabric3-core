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
package org.fabric3.binding.ws.metro.provision;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;

/**
 * Wire source definition for services that use WSDL-based contracts.
 */
public class MetroWsdlWireSourceDefinition extends MetroWireSourceDefinition {
    private static final long serialVersionUID = -1905843346636208650L;

    /**
     * Constructor.
     *
     * @param serviceUri         the structural service URI
     * @param endpointDefinition endpoint metadata
     * @param wsdl               the WSDL document as a string
     * @param intents            intents configured at the endpoint level that are provided natively by the Metro
     * @param bidirectional           true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers           optional binding handlers
     */
    public MetroWsdlWireSourceDefinition(URI serviceUri,
                                         ServiceEndpointDefinition endpointDefinition,
                                         String wsdl,
                                         List<QName> intents,
                                         boolean bidirectional,
                                         List<PhysicalBindingHandlerDefinition> handlers) {
        super(serviceUri, endpointDefinition, wsdl, intents, bidirectional, handlers);
        dataTypes.clear();
        dataTypes.add(PhysicalDataTypes.DOM);
    }

}