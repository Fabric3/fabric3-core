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

import java.util.List;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Base class for reference-side wire definitions.
 */
public abstract class MetroWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 5758003268658918242L;

    private ReferenceEndpointDefinition endpointDefinition;
    private ConnectionConfiguration connectionConfiguration;
    private String wsdl;
    private List<PhysicalBindingHandlerDefinition> handlers;
    private boolean bidirectional;

    /**
     * Constructor.
     *
     * @param endpointDefinition      endpoint metadata
     * @param wsdl                    the endpoint WSDL or null if the WSDL can be derived from the SEI without the need to merge policy
     * @param connectionConfiguration the HTTP configuration or null if defaults should be used
     * @param bidirectional           true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers                optional binding handlers
     */
    public MetroWireTargetDefinition(ReferenceEndpointDefinition endpointDefinition,
                                     String wsdl,
                                     ConnectionConfiguration connectionConfiguration,
                                     boolean bidirectional,
                                     List<PhysicalBindingHandlerDefinition> handlers) {
        this.endpointDefinition = endpointDefinition;
        this.wsdl = wsdl;
        this.connectionConfiguration = connectionConfiguration;
        this.bidirectional = bidirectional;
        this.handlers = handlers;
    }

    /**
     * Returns the endpoint information.
     *
     * @return the endpoint information
     */
    public ReferenceEndpointDefinition getEndpointDefinition() {
        return endpointDefinition;
    }

    /**
     * Returns the serialized WSDL for the target service.
     *
     * @return the serialized WSDL for the target service
     */
    public String getWsdl() {
        return wsdl;
    }

    /**
     * Returns the HTTP connection configuration.
     *
     * @return the HTTP connection configuration
     */
    public ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * True if this wire is bidirectional.
     *
     * @return true if this wire is bidirectional
     */
    public boolean isBidirectional() {
        return bidirectional;
    }

    /**
     * Returns the optional handlers to engage when processing an invocation.
     *
     * @return the optional handlers to engage when processing an invocation.
     */
    public List<PhysicalBindingHandlerDefinition> getHandlers() {
        return handlers;
    }
}
