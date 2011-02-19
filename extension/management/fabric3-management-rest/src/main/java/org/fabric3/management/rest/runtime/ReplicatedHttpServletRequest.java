/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.management.rest.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * Serialized servlet request used to replicate resource requests.
 *
 * @version $Rev: 9973 $ $Date: 2011-02-11 08:52:40 +0100 (Fri, 11 Feb 2011) $
 */
public class ReplicatedHttpServletRequest implements HttpServletRequest, Serializable {
    private static final long serialVersionUID = 4494355941648639187L;
    private StringBuffer requestUrl;
    private String requestUri;
    private String servletPath;

    private String method;
    private String contentType;
    private String protocol;
    private String scheme;
    private int port;
    private String address;
    private String serverName;

    public void setRequestUrl(StringBuffer requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLocalAddr(String address) {
        this.address = address;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getAuthType() {
        return null;
    }

    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    public long getDateHeader(String name) {
        return 0;
    }

    public String getHeader(String name) {
        return null;
    }

    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    public int getIntHeader(String name) {
        return -1;
    }

    public String getMethod() {
        return method;
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public String getQueryString() {
        return null;
    }

    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    public String getRequestURI() {
        return requestUri;
    }

    public StringBuffer getRequestURL() {
        return requestUrl;
    }

    public String getServletPath() {
        return servletPath;
    }

    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return Collections.emptyList();
    }

    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return contentType;
    }

    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getParameter(String name) {
        throw new UnsupportedOperationException();
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(Collections.<String>emptyList());
    }

    public String[] getParameterValues(String name) {
        return new String[0];
    }

    public Map<String, String[]> getParameterMap() {
        return Collections.emptyMap();
    }

    public String getProtocol() {
        return protocol;
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return 0;
    }

    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    public String getRemoteHost() {
        return null;
    }

    public void setAttribute(String name, Object o) {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException();
    }

    public boolean isSecure() {
        return "https".equals(protocol);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    public String getRealPath(String path) {
        return null;
    }

    public int getRemotePort() {
        throw new UnsupportedOperationException();
    }

    public String getLocalName() {
        throw new UnsupportedOperationException();
    }

    public String getLocalAddr() {
        return address;
    }

    public int getLocalPort() {
        return port;
    }

    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public boolean isAsyncStarted() {
        return false;
    }

    public boolean isAsyncSupported() {
        return false;
    }

    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }
}
