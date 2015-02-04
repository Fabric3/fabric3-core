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

/**
 * Encapsulates endpoint information for the service side of an invocation chain.
 */
public class ServiceEndpointDefinition extends AbstractEndpointDefinition {
    private static final long serialVersionUID = 3242092002688340187L;
    private URI servicePath;

    /**
     * Constructor.
     *
     * @param serviceName the qualified service name
     * @param portName    the qualified port name
     * @param servicePath the service path relative to the runtime base HTTP address
     * @param wsdl        the serialized WSDL this endpoint definition is derived from
     */
    public ServiceEndpointDefinition(QName serviceName, QName portName, URI servicePath, String wsdl) {
        super(serviceName, portName, wsdl);
        this.servicePath = servicePath;
    }

    /**
     * Constructor.
     *
     * @param serviceName the qualified service name
     * @param portName    the qualified port name
     * @param servicePath the service path relative to the runtime base HTTP address
     */
    public ServiceEndpointDefinition(QName serviceName, QName portName, URI servicePath) {
        this(serviceName, portName, servicePath, null);
    }

    /**
     * Returns the service path relative to the runtime base HTTP address.
     *
     * @return the service path relative to the runtime base HTTP address
     */
    public URI getServicePath() {
        return servicePath;
    }

}