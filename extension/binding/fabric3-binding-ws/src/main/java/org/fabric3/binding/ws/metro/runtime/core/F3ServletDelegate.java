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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

/**
 * Custom servlet delegate that supports lazy initiation of adapters.
 */
public class F3ServletDelegate extends WSServletDelegate {
    private static final String WS_TRANSFER_GET_ACTION = "\"http://schemas.xmlsoap.org/ws/2004/09/transfer/Get\"";

    private Map<String, ServletAdapter> adapters = new ConcurrentHashMap<>();
    private Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param servletContext Servlet context.
     */
    public F3ServletDelegate(ServletContext servletContext) {
        super(new ArrayList<ServletAdapter>(), servletContext);
        // turn off status page
        HttpAdapter.publishStatusPage = false;
    }

    /**
     * Registers a new servlet adapter. Each adapter corresponds to a provisioned service endpoint.
     *
     * @param servletAdapter servlet adapter to be registered.
     * @param classLoader    the TCCL to set on incoming requests
     */
    public void registerServletAdapter(ServletAdapter servletAdapter, ClassLoader classLoader) {
        String path = servletAdapter.urlPattern;
        adapters.put(path, servletAdapter);
        classLoaders.put(path, classLoader);
    }

    /**
     * Unregisters a servlet adapter.
     *
     * @param path the servlet adaptor path.
     * @return the unregistered adaptor or null
     */
    public ServletAdapter unregisterServletAdapter(String path) {
        classLoaders.remove(path);
        ServletAdapter adapter = adapters.remove(path);
        if (adapter != null) {
            adapter.getEndpoint().dispose();
        }
        return adapter;
    }

    @Override
    protected ServletAdapter getTarget(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return adapters.get(path);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String actionHeader = request.getHeader("SOAPAction");
        // Short-circuit processing GET and POSTs with a SoapAction of "http://schemas.xmlsoap.org/ws/2004/09/transfer/Get" unless they are
        // WS-MEX requests, i.e. <endpoint-url>/mex. This avoids an issue where WSIT is not properly setup to handle these requests using SOAP 1.2
        // and the WSIT client falls back to issuing a SOAP 1.1 request with a content type of text/xml, which causes then causes WSIT to throw a
        // content type error. A proper fix would be to enable this SoapAction with SOAP 1.2.
        if (!request.getRequestURI().endsWith("/mex") && actionHeader != null && WS_TRANSFER_GET_ACTION.equals(actionHeader)) {
            response.setStatus(200);
            return;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        ClassLoader classLoader = classLoaders.get(path);
        assert classLoader != null;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL to the service endpoint classloader
            Thread.currentThread().setContextClassLoader(classLoader);
            super.doPost(request, response, servletContext);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException {
        String query = request.getQueryString();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        if (query != null && (query.equals("WSDL") || query.startsWith("wsdl") || query.startsWith("xsd="))) {
            // For metadata requests (e.g. WSDLs, XSDs), the extension classloader must be set as the TCCL as Metro attempts to load
            // a StAX implementation to process documents. The StAX FactoryFinder uses the TCCL to search for the parser implementation class.
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                super.doGet(request, response, context);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        } else {
            // not a metadata request - e.g. a REST request
            super.doGet(request, response, context);
        }
    }
}
