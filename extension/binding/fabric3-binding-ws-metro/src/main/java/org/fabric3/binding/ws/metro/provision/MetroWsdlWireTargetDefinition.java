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
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;

/**
 * Reference-side wire target information defined by a WSDL document.
 */
public class MetroWsdlWireTargetDefinition extends MetroWireTargetDefinition {
    private static final long serialVersionUID = 5531927726014190158L;
    private static List<DataType> PHYSICAL_DATA_TYPES = new ArrayList<>();
    private String wsdl;

    static {
        PHYSICAL_DATA_TYPES.add(PhysicalDataTypes.DOM);
    }

    /**
     * Constructor.
     *
     * @param endpointDefinition      endpoint metadata
     * @param wsdl                    the serialized wsdl
     * @param intents                 intents configured at the endpoint level that are provided natively by the Metro
     * @param securityConfiguration   the security configuration or null if security is not configured
     * @param connectionConfiguration the HTTP configuration or null if defaults should be used
     * @param bidirectional           true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers                optional binding handlers
     */
    public MetroWsdlWireTargetDefinition(ReferenceEndpointDefinition endpointDefinition,
                                         String wsdl,
                                         List<QName> intents,
                                         SecurityConfiguration securityConfiguration,
                                         ConnectionConfiguration connectionConfiguration,
                                         boolean bidirectional,
                                         List<PhysicalBindingHandlerDefinition> handlers) {
        super(endpointDefinition, wsdl, intents, securityConfiguration, connectionConfiguration, bidirectional, handlers);
        this.wsdl = wsdl;
    }

    public String getWsdl() {
        return wsdl;
    }

    @Override
    public List<DataType> getDataTypes() {
        return PHYSICAL_DATA_TYPES;
    }

}