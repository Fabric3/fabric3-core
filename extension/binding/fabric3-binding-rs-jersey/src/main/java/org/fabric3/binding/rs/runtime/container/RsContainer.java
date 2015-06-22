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
package org.fabric3.binding.rs.runtime.container;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.rs.runtime.provider.NameBindingFilterProvider;
import org.fabric3.binding.rs.runtime.provider.ProviderRegistry;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Dispatches to resources under a common binding URI path defined in a deployable contribution. Specifically, all binding.rs resources configured with the same
 * URI.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public final class RsContainer extends HttpServlet {
    private static final long serialVersionUID = 1954697059021782141L;

    private String path;
    private ProviderRegistry providerRegistry;
    private NameBindingFilterProvider provider;

    private ServletContainer servlet;
    private ServletConfig servletConfig;
    private List<Resource> resources;

    public RsContainer(String path, ProviderRegistry providerRegistry, NameBindingFilterProvider provider) {
        this.path = path;
        this.providerRegistry = providerRegistry;
        this.provider = provider;
        this.resources = new ArrayList<>();
    }

    public void addResource(Resource resource) throws Fabric3Exception {
        resources.add(resource);
        reload();
    }

    public void init(ServletConfig config) {
        servletConfig = config;
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req = new HttpServletRequestWrapper(req);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            workContext.setHeader("fabric3.httpRequest", req);
            workContext.setHeader("fabric3.httpResponse", res);
            servlet.service(req, res);
        } catch (ServletException | IOException se) {
            se.printStackTrace();
            throw se;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ServletException(t);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            workContext.reset();
        }
    }

    private void reload() throws Fabric3Exception {
        try {
            // register contribution resources
            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(JacksonFeature.class);
            resourceConfig.register(MultiPartFeature.class);

            // configure filters
            Collection<Object> globalProviders = providerRegistry.getGlobalProvider();
            globalProviders.forEach(resourceConfig::register);
            resourceConfig.register(provider);

            resources.forEach(resourceConfig::registerResources);

            servlet = new ServletContainer(resourceConfig);
            servlet.init(servletConfig);
        } catch (Throwable t) {
            throw new Fabric3Exception(t);
        }
    }

    /**
     * Wraps the request to override {@link #getServletPath()} and always return the root path. This is necessary since Jersey routes requests to resources
     * based on the root servlet path. Since the RsContainer servlet is not the root servlet (binding.rs registers resources relative to the binding URI), the
     * servlet path must be overridden for Jersey to route correctly.
     */
    private class HttpServletRequestWrapper implements HttpServletRequest {
        private HttpServletRequest delegate;

        private HttpServletRequestWrapper(HttpServletRequest delegate) {
            this.delegate = delegate;
        }

        public String getAuthType() {
            return delegate.getAuthType();
        }

        public Cookie[] getCookies() {
            return delegate.getCookies();
        }

        public long getDateHeader(String name) {
            return delegate.getDateHeader(name);
        }

        public String getHeader(String name) {
            return delegate.getHeader(name);
        }

        public Enumeration<String> getHeaders(String name) {
            return delegate.getHeaders(name);
        }

        public Enumeration<String> getHeaderNames() {
            return delegate.getHeaderNames();
        }

        public int getIntHeader(String name) {
            return delegate.getIntHeader(name);
        }

        public String getMethod() {
            return delegate.getMethod();
        }

        public String getPathInfo() {
            return delegate.getPathInfo();
        }

        public String getPathTranslated() {
            return delegate.getPathTranslated();
        }

        public String getContextPath() {
            return path;
        }

        public String getQueryString() {
            return delegate.getQueryString();
        }

        public String getRemoteUser() {
            return delegate.getRemoteUser();
        }

        public boolean isUserInRole(String role) {
            return delegate.isUserInRole(role);
        }

        public Principal getUserPrincipal() {
            return delegate.getUserPrincipal();
        }

        public String getRequestedSessionId() {
            return delegate.getRequestedSessionId();
        }

        public String getRequestURI() {
            return delegate.getRequestURI();
        }

        public StringBuffer getRequestURL() {
            return delegate.getRequestURL();
        }

        public String getServletPath() {
            return "";
        }

        public HttpSession getSession(boolean create) {
            return delegate.getSession(create);
        }

        public HttpSession getSession() {
            return delegate.getSession();
        }

        public boolean isRequestedSessionIdValid() {
            return delegate.isRequestedSessionIdValid();
        }

        public boolean isRequestedSessionIdFromCookie() {
            return delegate.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL() {
            return delegate.isRequestedSessionIdFromURL();
        }

        public boolean isRequestedSessionIdFromUrl() {
            return delegate.isRequestedSessionIdFromUrl();
        }

        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            return delegate.authenticate(response);
        }

        public void login(String username, String password) throws ServletException {
            delegate.login(username, password);
        }

        public void logout() throws ServletException {
            delegate.logout();
        }

        public Collection<Part> getParts() throws IOException, ServletException {
            return delegate.getParts();
        }

        public Part getPart(String name) throws IOException, ServletException {
            return delegate.getPart(name);
        }

        public Object getAttribute(String name) {
            return delegate.getAttribute(name);
        }

        public Enumeration<String> getAttributeNames() {
            return delegate.getAttributeNames();
        }

        public String getCharacterEncoding() {
            return delegate.getCharacterEncoding();
        }

        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            delegate.setCharacterEncoding(env);
        }

        public int getContentLength() {
            return delegate.getContentLength();
        }

        public String getContentType() {
            return delegate.getContentType();
        }

        public ServletInputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        public String getParameter(String name) {
            return delegate.getParameter(name);
        }

        public Enumeration<String> getParameterNames() {
            return delegate.getParameterNames();
        }

        public String[] getParameterValues(String name) {
            return delegate.getParameterValues(name);
        }

        public Map<String, String[]> getParameterMap() {
            return delegate.getParameterMap();
        }

        public String getProtocol() {
            return delegate.getProtocol();
        }

        public String getScheme() {
            return delegate.getScheme();
        }

        public String getServerName() {
            return delegate.getServerName();
        }

        public int getServerPort() {
            return delegate.getServerPort();
        }

        public BufferedReader getReader() throws IOException {
            return delegate.getReader();
        }

        public String getRemoteAddr() {
            return delegate.getRemoteAddr();
        }

        public String getRemoteHost() {
            return delegate.getRemoteHost();
        }

        public void setAttribute(String name, Object o) {
            delegate.setAttribute(name, o);
        }

        public void removeAttribute(String name) {
            delegate.removeAttribute(name);
        }

        public Locale getLocale() {
            return delegate.getLocale();
        }

        public Enumeration<Locale> getLocales() {
            return delegate.getLocales();
        }

        public boolean isSecure() {
            return delegate.isSecure();
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return delegate.getRequestDispatcher(path);
        }

        public String getRealPath(String path) {
            return delegate.getRealPath(path);
        }

        public int getRemotePort() {
            return delegate.getRemotePort();
        }

        public String getLocalName() {
            return delegate.getLocalName();
        }

        public String getLocalAddr() {
            return delegate.getLocalAddr();
        }

        public int getLocalPort() {
            return delegate.getLocalPort();
        }

        public ServletContext getServletContext() {
            return delegate.getServletContext();
        }

        public AsyncContext startAsync() throws IllegalStateException {
            return delegate.startAsync();
        }

        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            return delegate.startAsync(servletRequest, servletResponse);
        }

        public boolean isAsyncStarted() {
            return delegate.isAsyncStarted();
        }

        public boolean isAsyncSupported() {
            return delegate.isAsyncSupported();
        }

        public AsyncContext getAsyncContext() {
            return delegate.getAsyncContext();
        }

        public DispatcherType getDispatcherType() {
            return delegate.getDispatcherType();
        }
    }

}
