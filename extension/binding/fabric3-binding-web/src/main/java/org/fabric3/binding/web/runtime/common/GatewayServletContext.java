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
package org.fabric3.binding.web.runtime.common;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.atmosphere.cpr.DefaultAsyncSupportResolver;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;

public class GatewayServletContext implements ServletContext {
    private final static String WEB_INF = "/WEB-INF/classes/";
    private String contextPath;
    private Map<String, String> initParams = new HashMap<>();
	private String serverInfo = "Fabric3";

    public GatewayServletContext(String contextPath, ClassLoaderRegistry classLoaderRegistry) {
        this.contextPath = contextPath;
        attachJettyIfAny(classLoaderRegistry);
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParams.keySet());
    }

    public boolean setInitParameter(String name, String value) {
        if (initParams.containsKey(name)) {
            return false;
        }
        initParams.put(name, value);
        return true;
    }

    public Set<String> getResourcePaths(String path) {
        return Collections.emptySet();
    }

    public URL getResource(String path) throws MalformedURLException {
        if (WEB_INF.equals(path)) {
            return new URL("");
        }
        return null;
    }

    public ServletContext getContext(String uripath) {
        return null;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public String getServletContextName() {
        return "Fabric3";
    }

    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return null;
    }

    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    public void addListener(String className) {

    }

    public <T extends EventListener> void addListener(T t) {

    }

    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public void declareRoles(String... roleNames) {

    }

    public int getMajorVersion() {
        return 3;
    }

    public int getMinorVersion() {
        return 0;
    }

    public int getEffectiveMajorVersion() {
        return 0;
    }

    public int getEffectiveMinorVersion() {
        return 0;
    }

    public String getMimeType(String file) {
        return null;
    }

    public InputStream getResourceAsStream(String path) {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    public Enumeration<Servlet> getServlets() {
        return null;
    }

    public Enumeration<String> getServletNames() {
        return null;
    }

    public void log(String msg) {

    }

    public void log(Exception exception, String msg) {

    }

    public void log(String message, Throwable throwable) {

    }

    public String getRealPath(String path) {
        return null;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        return null;
    }

    public void setAttribute(String name, Object object) {

    }

    public void removeAttribute(String name) {

    }

	public void setServerInfo(String serverInfo) {
		this.serverInfo = serverInfo;
	}
	
	private void attachJettyIfAny(ClassLoaderRegistry classLoaderRegistry) {
    	String[] jettySupport = {DefaultAsyncSupportResolver.JETTY,  
    			                 DefaultAsyncSupportResolver.JETTY_7 ,
    			                 DefaultAsyncSupportResolver.JETTY_8
    			                 };
    	int bestMatch = 0;
    	ClassLoader jettyCandidate = null;
    	ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    	if (contextClassLoader instanceof MultiParentClassLoader){
    		Map<URI, ClassLoader> map = classLoaderRegistry.getClassLoaders();
    		Collection<ClassLoader> classLoaders = map.values();
    		for (ClassLoader cl : classLoaders) {
    			if (cl != contextClassLoader) {
    				int currentMatch = 0;
	    			for (String className : jettySupport) {	    				
	    				try {
							cl.loadClass(className);
							currentMatch ++;
							if (currentMatch > bestMatch) {
								jettyCandidate = cl;
								bestMatch = currentMatch;
							}
						} catch (ClassNotFoundException e) {
							continue;
						}
	    			}		
	    		}		
			}
    		if (jettyCandidate!=null && jettyCandidate instanceof MultiParentClassLoader){
    			((MultiParentClassLoader)contextClassLoader).addExtensionClassLoader((MultiParentClassLoader) jettyCandidate);
    			serverInfo = "JETTY.8.0.0";
    		}
    	}
    }
}
