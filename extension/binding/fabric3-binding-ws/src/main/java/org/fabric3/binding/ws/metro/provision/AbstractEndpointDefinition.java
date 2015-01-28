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
import java.io.Serializable;

/**
 * Encapsulates endpoint information for the reference side of an invocation chain.
 */
public abstract class AbstractEndpointDefinition implements Serializable {
    private static final long serialVersionUID = -8322624061436929156L;
    private QName serviceName;
    private QName portName;
    private String wsdl;

    /**
     * Constructor.
     *
     * @param serviceName the qualified name of the target service
     * @param portName    the port name
     * @param wsdl        the serialized wsdl
     */
    public AbstractEndpointDefinition(QName serviceName, QName portName, String wsdl) {
        this.serviceName = serviceName;
        this.portName = portName;
        this.wsdl = wsdl;
    }

    /**
     * Returns the qualified service name.
     *
     * @return the qualified service name
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * Returns the qualified port name.
     *
     * @return the qualified port name
     */
    public QName getPortName() {
        return portName;
    }

    /**
     * Returns a serialized WSDL specified using wsdlElement or wsdlLocation, or null if one is not specified. This WSDL may be overriden by a generated one if
     * policy is specified on the reference. Otherwise, it should be used to create JAX-WS reference proxies.
     *
     * @return the serialized WSDL or null
     */
    public String getWsdl() {
        return wsdl;
    }
}
