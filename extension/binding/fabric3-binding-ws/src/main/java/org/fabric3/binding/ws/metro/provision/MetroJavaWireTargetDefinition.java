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

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;

/**
 * Reference-side wire target information defined by a Java interface.
 */
public class MetroJavaWireTargetDefinition extends MetroWireTargetDefinition {
    private static final long serialVersionUID = 5332578680612891881L;

    private Map<String, String> schemas;
    private URL wsdlLocation;
    private Class<?> interfaze;
    private int retries;

    /**
     * Constructor.
     *
     * @param endpointDefinition      endpoint metadata
     * @param interfaze               the service contract (SEI)
     * @param wsdl                    the generated WSDL or null if the WSDL can be derived from the SEI or is not explicitly specified via wsdlElement or
     *                                wsdlLocation
     * @param schemas                 the generated schemas or null
     * @param wsdlLocation            optional URL to the WSDL location
     * @param connectionConfiguration the HTTP configuration or null if defaults should be used
     * @param retries                 the number of retries to attempt in the event the service is unavailable when an invocation is made
     * @param bidirectional           true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers                optional binding handlers
     */
    public MetroJavaWireTargetDefinition(ReferenceEndpointDefinition endpointDefinition,
                                         Class<?> interfaze,
                                         String wsdl,
                                         Map<String, String> schemas,
                                         URL wsdlLocation,
                                         ConnectionConfiguration connectionConfiguration,
                                         int retries,
                                         boolean bidirectional,
                                         List<PhysicalBindingHandlerDefinition> handlers) {
        super(endpointDefinition, wsdl, connectionConfiguration, bidirectional, handlers);
        this.schemas = schemas;
        this.wsdlLocation = wsdlLocation;
        this.interfaze = interfaze;
        this.retries = retries;
    }

    /**
     * Returns the service contract name.
     *
     * @return the service contract name
     */
    public Class<?> getInterface() {
        return interfaze;
    }

    /**
     * Returns any associated WSDLs with the schemas.
     *
     * @return any associated WSDLs with the schemas
     */
    public Map<String, String> getSchemas() {
        return schemas;
    }

    /**
     * Returns an optional URL to the WSDL document.
     *
     * @return optional URL to the WSDL document
     */
    public URL getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * The number of retries in the event the target service is unavailable during an invocation.
     *
     * @return the number of retries
     */
    public int getRetries() {
        return retries;
    }
}