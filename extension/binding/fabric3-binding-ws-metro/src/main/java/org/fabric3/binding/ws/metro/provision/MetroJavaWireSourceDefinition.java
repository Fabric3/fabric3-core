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

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;

/**
 * Wire source definition for services that use Java interface-based contracts.
 */
public class MetroJavaWireSourceDefinition extends MetroWireSourceDefinition {
    private static final long serialVersionUID = 2898989563911925959L;

    private String interfaze;
    private byte[] generatedInterface;
    private URI classLoaderUri;
    private Map<String, String> schemas;

    private URL wsdlLocation;

    /**
     * Constructor.
     *
     * @param serviceUri         the structural service URI
     * @param endpointDefinition endpoint metadata
     * @param interfaze          the service contract (SEI) name.
     * @param generatedInterface the generated SEI bytes or null if no interface needed to be generated
     * @param classLoaderUri     the classloader for SEI types
     * @param wsdl               the generated WSDL containing merged policy or null if no policy applies to the endpoint
     * @param schemas            the schemas imported by the generated WSDL or null
     * @param wsdlLocation       optional URL to the WSDL location
     * @param bidirectional      true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers           optional binding handlers
     */
    public MetroJavaWireSourceDefinition(URI serviceUri,
                                         ServiceEndpointDefinition endpointDefinition,
                                         String interfaze,
                                         byte[] generatedInterface,
                                         URI classLoaderUri,
                                         String wsdl,
                                         Map<String, String> schemas,
                                         URL wsdlLocation,
                                         boolean bidirectional,
                                         List<PhysicalBindingHandlerDefinition> handlers) {
        super(serviceUri, endpointDefinition, wsdl, bidirectional, handlers);
        this.interfaze = interfaze;
        this.generatedInterface = generatedInterface;
        this.classLoaderUri = classLoaderUri;
        this.schemas = schemas;
        this.wsdlLocation = wsdlLocation;
    }

    /**
     * Returns the service contract name.
     *
     * @return the service contract name
     */
    public String getInterface() {
        return interfaze;
    }

    /**
     * Returns the generated SEI interface bytes.
     *
     * @return the generated SEI interface bytes
     */
    public byte[] getGeneratedInterface() {
        return generatedInterface;
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
     * Returns the SEI classloader URI.
     *
     * @return the SEI classloader URI
     */
    public URI getSEIClassLoaderUri() {
        return classLoaderUri;
    }
}