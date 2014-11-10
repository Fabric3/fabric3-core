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

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Encapsulates endpoint information for the reference side of an invocation chain.
 */
public class ReferenceEndpointDefinition extends AbstractEndpointDefinition {
    private static final long serialVersionUID = -7422624061436929193L;

    public static URL DYNAMIC_URL;

    static {
        try {
            DYNAMIC_URL = new URL("http://dynamic");
        } catch (MalformedURLException e) {
            DYNAMIC_URL = null;
        }
    }

    private boolean defaultServiceName;
    private QName portTypeName;
    private URL url;
    private boolean rpcLit;
    private transient Definition definition;

    /**
     * Constructor.
     *
     * @param serviceName        the qualified name of the target service
     * @param defaultServiceName true if the service name is a default and may be overriden by introspecting WSDL
     * @param portName           the port name
     * @param portTypeName       the qualified name of the target port type
     * @param url                the endpoint URL
     */
    public ReferenceEndpointDefinition(QName serviceName, boolean defaultServiceName, QName portName, QName portTypeName, URL url) {
        super(serviceName, portName, null);
        this.defaultServiceName = defaultServiceName;
        this.portTypeName = portTypeName;
        this.url = url;
    }

    public ReferenceEndpointDefinition(QName serviceName, boolean defaultServiceName, QName portName, QName portTypeName, URL url, String wsdl) {
        super(serviceName, portName, wsdl);
        this.defaultServiceName = defaultServiceName;
        this.portTypeName = portTypeName;
        this.url = url;
    }

    /**
     * Returns true if the service name is a default and may be overridden by introspecting WSDL. If a service name is not specified using the
     * <code>WebService.serviceName</code> annotation attribute, it is calculated according to JAX-WS mapping rules. However, web services stacks such as WCF
     * (.NET) adopt different defaulting schemes. To accommodate this, during reference proxy creation, the Fabric3 runtime will introspect the target WSDL to
     * determine the actual service name if the default name is not valid. The actual name will be determined by mapping the portType name to a service defined
     * in the WSDL. Note this can only be done if the WSDL contains exactly one service that uses the portType. Otherwise, a serviceName will need to be
     * explicitly specified using the <code>WebService</code> annotation or wsdlElement attribute of binding.ws.
     *
     * @return true if the service name is a default and may be overriden by introspecting WSDL
     */
    public boolean isDefaultServiceName() {
        return defaultServiceName;
    }

    /**
     * Returns the qualified port type name.
     *
     * @return the qualified port type name
     */
    public QName getPortTypeName() {
        return portTypeName;
    }

    /**
     * Returns the endpoint URL.
     *
     * @return the endpoint URL
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets if this endpoint uses RPC/Lit.
     *
     * @param rpcLit true if this endpoint uses RPC/Lit
     */
    public void setRpcLit(boolean rpcLit) {
        this.rpcLit = rpcLit;
    }

    /**
     * True if this endpoint uses RPC/Lit.
     *
     * @return true if this endpoint uses RPC/Lit
     */
    public boolean isRpcLit() {
        return rpcLit;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }
}
