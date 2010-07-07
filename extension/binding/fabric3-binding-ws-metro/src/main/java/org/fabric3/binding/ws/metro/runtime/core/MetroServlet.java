/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.ws.metro.runtime.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

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
 * Handles incoming HTTP requests and dispatches them to the Metro stack. Extends the Metro servlet and overrides the <code>getDelegate</code>
 * method.
 *
 * @version $Rev$ $Date$
 */
public class MetroServlet extends WSServlet {
    private static final long serialVersionUID = -2581439830158433922L;
    private static final String MEX_SUFFIX = "/mex";

    private ExecutorService executorService;
    private SecurityEnvironment securityEnvironment;

    private List<EndpointConfiguration> configurations = new ArrayList<EndpointConfiguration>();
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

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();
        // Setup the WSIT endpoint that handles WS-MEX requests for registered endpoints. The TCCL must be set for JAXB.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader seiClassLoader = MEXEndpoint.class.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(seiClassLoader);
            container = new F3Container(servletContext, securityEnvironment);

            WSBinding binding = BindingImpl.create(BindingID.SOAP12_HTTP);
            mexEndpoint = WSEndpoint.create(MEXEndpoint.class,
                                            false,
                                            null,
                                            null,
                                            null,
                                            container,
                                            binding,
                                            null,
                                            null,
                                            null,
                                            true);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        // register services
        for (EndpointConfiguration configuration : configurations) {
            registerService(configuration);
        }
    }

    public void registerService(EndpointConfiguration configuration) {
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
                metadata = new ArrayList<SDDocumentSource>();
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
            WSEndpoint<?> wsEndpoint = WSEndpoint.create(seiClass,
                                                         false,
                                                         invoker,
                                                         serviceName,
                                                         portName,
                                                         endpointContainer,
                                                         binding,
                                                         primaryWsdl,
                                                         metadata,
                                                         null,
                                                         true);
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
    public void unregisterService(String path) {
        if (delegate == null) {
            // case where the endpoint is undeployed before it has been activated
            for (Iterator<EndpointConfiguration> it = configurations.iterator(); it.hasNext();) {
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

}
