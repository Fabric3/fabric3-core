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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.ws.metro.runtime.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.mex.server.MEXEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;
import com.sun.xml.wss.SecurityEnvironment;

/**
 * Handles incoming HTTP requests and dispatches them to the Metro stack. Extends the Metro servlet and overrides the <code>getDelegate</code> method.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class MetroServlet extends WSServlet {
    private static final long serialVersionUID = -2581439830158433922L;
    private static final String MEX_SUFFIX = "/mex";

    private ExecutorService executorService;
    private SecurityEnvironment securityEnvironment;

    private List<EndpointConfiguration> configurations = new ArrayList<>();
    private ServletAdapterFactory servletAdapterFactory = new ServletAdapterFactory();
    private volatile F3ServletDelegate delegate;
    private F3Container container;
    private WSEndpoint<?> mexEndpoint;

    /**
     * Constructor
     *
     * @param executorService     the executor service for dispatching invocations
     * @param securityEnvironment the Fabric3 implementation of the Metro SecurityEnvironment SPI
     */
    public MetroServlet(ExecutorService executorService, SecurityEnvironment securityEnvironment) {
        this.executorService = executorService;
        this.securityEnvironment = securityEnvironment;
    }

    public synchronized void init(ServletConfig servletConfig) throws ServletException {
        if (delegate != null) {
            return;
        }
        super.init(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();
        // Setup the WSIT endpoint that handles WS-MEX requests for registered endpoints. The TCCL must be set for JAXB.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader seiClassLoader = MEXEndpoint.class.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(seiClassLoader);
            container = new F3Container(servletContext, securityEnvironment);

            WSBinding binding = BindingImpl.create(BindingID.SOAP12_HTTP);
            mexEndpoint = WSEndpoint.create(MEXEndpoint.class, false, null, null, null, container, binding, null, null, null, true);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        // register services
        for (EndpointConfiguration configuration : configurations) {
            registerService(configuration);
        }
    }

    public synchronized void registerService(EndpointConfiguration configuration) {
        if (delegate == null) {
            // servlet has not be initialized, delay service registration
            configurations.add(configuration);
            return;
        }
        Class<?> seiClass = configuration.getSeiClass();
        ClassLoader classLoader = seiClass.getClassLoader();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            URL wsdlLocation = configuration.getWsdlLocation();
            SDDocumentSource primaryWsdl = null;
            if (wsdlLocation != null) {
                // WSDL may not be defined for a Java-based endpoint, in which case it will be introspected from the SEI class
                primaryWsdl = SDDocumentSource.create(wsdlLocation);
            }
            BindingID bindingId = configuration.getBindingId();
            WebServiceFeature[] features = configuration.getFeatures();
            WSBinding binding = BindingImpl.create(bindingId, features);
            Container endpointContainer = container;
            List<SDDocumentSource> metadata = null;
            URL generatedWsdl = configuration.getGeneratedWsdl();
            if (generatedWsdl != null) {
                // create a container wrapper used by Metro to resolve the WSIT configuration
                endpointContainer = new WsitConfigurationContainer(container, generatedWsdl);
                // Compile the list of imported schemas so they can be resolved using ?xsd GET requests. Metro will re-write the WSDL import
                // so clients can dereference the imports when they obtain the WSDL.
                metadata = new ArrayList<>();
                List<URL> schemas = configuration.getGeneratedSchemas();
                if (schemas != null) {
                    for (URL schema : schemas) {
                        metadata.add(SDDocumentSource.create(schema));
                    }
                }
            }
            String servicePath = configuration.getServicePath();
            Invoker invoker = configuration.getInvoker();
            QName serviceName = configuration.getServiceName();
            QName portName = configuration.getPortName();

            // Fetch the handlers 
            loadHandlers(binding, configuration);

            WSEndpoint<?> wsEndpoint;
            try {
                wsEndpoint = WSEndpoint.create(seiClass, false, invoker, serviceName, portName, endpointContainer, binding, primaryWsdl, metadata, null, true);
            } catch (WebServiceException e) {
                if (e.getMessage().contains("Not a primary WSDL")) {
                    // workaround for WSDLs without service declarations
                    wsEndpoint = WSEndpoint.create(seiClass, false, invoker, serviceName, portName, endpointContainer, binding, null, metadata, null, true);
                } else {
                    throw e;
                }
            }
            wsEndpoint.setExecutor(executorService);

            ServletAdapter adapter = servletAdapterFactory.createAdapter(servicePath, servicePath, wsEndpoint);
            delegate.registerServletAdapter(adapter, F3Provider.class.getClassLoader());

            String mexPath = servicePath + MEX_SUFFIX;
            ServletAdapter mexAdapter = servletAdapterFactory.createAdapter(mexPath, mexPath, mexEndpoint);
            delegate.registerServletAdapter(mexAdapter, F3Provider.class.getClassLoader());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Unregisters a service endpoint.
     *
     * @param path the endpoint path
     */
    public synchronized void unregisterService(String path) {
        if (delegate == null) {
            // case where the endpoint is undeployed before it has been activated
            for (Iterator<EndpointConfiguration> it = configurations.iterator(); it.hasNext(); ) {
                EndpointConfiguration configuration = it.next();
                if (configuration.getServicePath().equals(path)) {
                    it.remove();
                    return;
                }
            }
            return;
        }
        ServletAdapter adapter = delegate.unregisterServletAdapter(path);
        if (adapter != null) {
            container.removeEndpoint(adapter);
        }
    }

    /**
     * Gets the {@link WSServletDelegate} that we will be forwarding the requests to.
     *
     * @return Returns a Fabric3 servlet delegate.
     */
    protected WSServletDelegate getDelegate(ServletConfig servletConfig) {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = new F3ServletDelegate(servletConfig.getServletContext());
                }
            }
        }
        return delegate;
    }

    private void loadHandlers(Binding binding, EndpointConfiguration config) {
        List<Handler> handlers = config.getHandlers();
        if (handlers == null) {
            return;
        }
        binding.setHandlerChain(handlers);
    }

}
