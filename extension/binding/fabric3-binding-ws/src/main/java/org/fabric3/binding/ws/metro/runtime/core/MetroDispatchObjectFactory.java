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
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;

/**
 * Creates JAX-WS <code>Dispatch</code> instances that can be shared among wire invocation chains.
 */
public class MetroDispatchObjectFactory extends AbstractMetroBindingProviderFactory<Dispatch<Source>> {
    private QName serviceName;
    private QName portName;
    private WebServiceFeature[] features;
    private File wsitConfiguration;
    private ExecutorService executorService;
    private Dispatch<Source> dispatch;
    private URL wsdlLocation;

    /**
     * Constructor.
     *
     * @param endpointDefinition      the target endpoint definition
     * @param wsdlLocation            the WSDL defining the target service contract
     * @param wsitConfiguration       WSIT policy configuration for the proxy, or null if policy is not configured
     * @param connectionConfiguration the underlying HTTP connection configuration or null if defaults should be used
     * @param handlers                messages handlers or null
     * @param features                web services features to enable on the generated proxy
     * @param executorService         the executor service used for dispatching invocations
     */
    public MetroDispatchObjectFactory(ReferenceEndpointDefinition endpointDefinition,
                                      URL wsdlLocation,
                                      File wsitConfiguration,
                                      ConnectionConfiguration connectionConfiguration,
                                      List<Handler> handlers,
                                      WebServiceFeature[] features,
                                      ExecutorService executorService) {
        super(connectionConfiguration, handlers);
        this.wsdlLocation = wsdlLocation;
        this.serviceName = endpointDefinition.getServiceName();
        this.portName = endpointDefinition.getPortName();
        this.features = features;
        this.wsitConfiguration = wsitConfiguration;
        this.executorService = executorService;
    }

    public Dispatch<Source> getInstance() throws ObjectCreationException {
        if (dispatch == null) {
            // there is a possibility more than one proxy will be created but since this does not have side-effects, avoid synchronization
            dispatch = createProxy();
        }
        return dispatch;
    }

    /**
     * Lazily creates the service proxy. Proxy creation is done during the first invocation as the target service may not be available when the client that the
     * proxy is to be injected into is instantiated. The proxy is later cached for subsequent invocations.
     *
     * @return the web service proxy
     * @throws ObjectCreationException if there was an error creating the proxy
     */
    private Dispatch<Source> createProxy() throws ObjectCreationException {
        try {
            Service service;
            WSService.InitParams params = new WSService.InitParams();
            WsitClientConfigurationContainer container;
            if (wsitConfiguration != null) {
                container = new WsitClientConfigurationContainer(wsitConfiguration.toURI().toURL());
            } else {
                // No policy
                container = new WsitClientConfigurationContainer();
            }
            params.setContainer(container);
            service = WSService.create(wsdlLocation, serviceName, params);
            // use the kernel scheduler for dispatching
            service.setExecutor(executorService);
            Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD, features);
            configureConnection(dispatch);
            configureHandlers(dispatch);
            setSOAPAction(dispatch);
            return dispatch;
        } catch (InaccessibleWSDLException | MalformedURLException e) {
            throw new ObjectCreationException(e);
        }

    }

    private void setSOAPAction(Dispatch<Source> dispatch) {
        Map<String, Object> context = dispatch.getRequestContext();
        context.put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        context.put(BindingProvider.SOAPACTION_URI_PROPERTY, "");
    }

}