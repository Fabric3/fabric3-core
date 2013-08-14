/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
 */
package org.fabric3.binding.rs.runtime;

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

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;
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

    private ServletContainer servlet;
    private ServletConfig servletConfig;
    private List<Resource> resources;
    private String path;

    public RsContainer(String path) {
        this.path = path;
        this.resources = new ArrayList<Resource>();
    }

    public void addResource(Resource resource) throws RsContainerException {
        resources.add(resource);
        reload();
    }

    public void init(ServletConfig config) {
        servletConfig = config;
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
        } catch (ServletException se) {
            se.printStackTrace();
            throw se;
        } catch (IOException ie) {
            ie.printStackTrace();
            throw ie;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ServletException(t);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            workContext.reset();
        }
    }

    private void reload() throws RsContainerException {
        try {
            // register contribution resources
            ResourceConfig resourceConfig = new ResourceConfig();
            for (Resource resource : resources) {
                resourceConfig.registerResources(resource);
            }
            servlet = new ServletContainer(resourceConfig);
            servlet.init(servletConfig);
        } catch (ServletException e) {
            throw new RsContainerException(e);
        } catch (Throwable t) {
            RsContainerException e = new RsContainerException(t);
            throw e;
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
