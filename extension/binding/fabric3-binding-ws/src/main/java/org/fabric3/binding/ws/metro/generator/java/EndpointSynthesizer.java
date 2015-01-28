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
package org.fabric3.binding.ws.metro.generator.java;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * Synthesizes endpoint information from a Java service contract.
 */
public interface EndpointSynthesizer {

    /**
     * Synthesize reference endpoint information.
     *
     * @param contract     the service contract
     * @param serviceClass the service endpoint implementation
     * @param url          the target endpoint URL
     * @return the     endpoint information
     */
    ReferenceEndpointDefinition synthesizeReferenceEndpoint(JavaServiceContract contract, Class<?> serviceClass, URL url);

    /**
     * Synthesize service endpoint information.
     *
     * @param contract     the service contract
     * @param serviceClass the service endpoint implementation
     * @param servicePath  the relative service path
     * @return the     endpoint information
     */
    ServiceEndpointDefinition synthesizeServiceEndpoint(JavaServiceContract contract, Class<?> serviceClass, URI servicePath);

}
