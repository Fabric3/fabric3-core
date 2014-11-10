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
package org.fabric3.binding.ws.metro.runtime.core;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.net.URI;
import java.net.URL;
import java.util.List;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.server.Invoker;

/**
 * Configuration for provisioning a service as a web service endpoint.
 */
public class EndpointConfiguration {
    private URI serviceUri;
    private Class<?> seiClass;
    private QName serviceName;
    private QName portName;
    private String servicePath;
    private Invoker invoker;
    private WebServiceFeature[] features;
    private BindingID bindingId;
    private URL generatedWsdl;
    private List<URL> generatedSchemas;
    private URL wsdlLocation;
    private List<Handler> handlers;

    /**
     * Constructor that takes a WSDL document at a given URL. If the URL is null, a WSDL will be generated from the service endpoint interface.
     *
     * @param serviceUri       the structural service URI
     * @param seiClass         service endpoint interface.
     * @param serviceName      service name
     * @param portName         port name
     * @param servicePath      Relative path on which the service is provisioned.
     * @param wsdlLocation     URL to the WSDL document.
     * @param invoker          Invoker for receiving the web service request.
     * @param features         Web service features to enable.
     * @param bindingId        Binding ID to use.
     * @param generatedWsdl    the generated WSDL used for WSIT configuration or null if no policy is configured
     * @param generatedSchemas the handles to schemas (XSDs) imported by the WSDL or null if none exist
     * @param handlers         the binding handlers, may be null
     */
    public EndpointConfiguration(URI serviceUri,
                                 Class<?> seiClass,
                                 QName serviceName,
                                 QName portName,
                                 String servicePath,
                                 URL wsdlLocation,
                                 Invoker invoker,
                                 WebServiceFeature[] features,
                                 BindingID bindingId,
                                 URL generatedWsdl,
                                 List<URL> generatedSchemas,
                                 List<Handler> handlers) {
        this.serviceUri = serviceUri;
        this.seiClass = seiClass;
        this.serviceName = serviceName;
        this.portName = portName;
        this.servicePath = servicePath;
        this.wsdlLocation = wsdlLocation;
        this.invoker = invoker;
        this.features = features;
        this.bindingId = bindingId;
        this.generatedWsdl = generatedWsdl;
        this.generatedSchemas = generatedSchemas;
        this.handlers = handlers;
    }

    public URL getWsdlLocation() {
        return wsdlLocation;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public QName getPortName() {
        return portName;
    }

    public String getServicePath() {
        return servicePath;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public WebServiceFeature[] getFeatures() {
        return features;
    }

    public BindingID getBindingId() {
        return bindingId;
    }

    public URL getGeneratedWsdl() {
        return generatedWsdl;
    }

    public List<URL> getGeneratedSchemas() {
        return generatedSchemas;
    }

    public Class<?> getSeiClass() {
        return seiClass;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public URI getServiceUri() {
        return serviceUri;
    }
}