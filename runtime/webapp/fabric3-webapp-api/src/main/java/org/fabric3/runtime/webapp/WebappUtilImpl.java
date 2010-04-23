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
package org.fabric3.runtime.webapp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import javax.management.MBeanServer;
import javax.servlet.ServletContext;

import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.jmx.agent.ManagementException;
import static org.fabric3.runtime.webapp.Constants.MONITOR_FACTORY_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.MONITOR_FACTORY_PARAM;

/**
 * @version $Rev$ $Date$
 */
public class WebappUtilImpl implements WebappUtil {

    private static final String SYSTEM_CONFIG = "/WEB-INF/systemConfig.xml";
    private static final String RUNTIME_CLASS = "org.fabric3.runtime.webapp.WebappRuntimeImpl";
    private static final String COORDINATOR_CLASS = "org.fabric3.fabric.runtime.DefaultCoordinator";
    private static final String SYSETM_COMPOSITE = "META-INF/fabric3/webapp.composite";

    private final ServletContext servletContext;

    public WebappUtilImpl(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public RuntimeCoordinator getCoordinator(BootConfiguration configuration, ClassLoader bootClassLoader) throws Fabric3InitException {
        try {
            Class<?> clazz = bootClassLoader.loadClass(COORDINATOR_CLASS);
            Constructor<?> ctor = clazz.getConstructor(BootConfiguration.class);
            return (RuntimeCoordinator) ctor.newInstance(configuration);
        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException(e);
        } catch (InvocationTargetException e) {
            throw new Fabric3InitException(e);
        } catch (NoSuchMethodException e) {
            throw new Fabric3InitException(e);
        }
    }

    public URL getSystemScdl(ClassLoader bootClassLoader) throws InvalidResourcePath {
        try {
            return convertToURL(SYSETM_COMPOSITE, bootClassLoader);
        } catch (MalformedURLException e) {
            throw new InvalidResourcePath("Webapp system composite", SYSETM_COMPOSITE, e);
        }
    }

    public Source getSystemConfig() throws InvalidResourcePath {
        try {
            URL url = servletContext.getResource(SYSTEM_CONFIG);
            return new UrlSource(url);
        } catch (MalformedURLException e) {
            throw new InvalidResourcePath("Webapp system composite", SYSETM_COMPOSITE, e);
        }
    }

    public String getInitParameter(String name, String value) {
        String result = servletContext.getInitParameter(name);
        if (result != null && result.length() != 0) {
            return result;
        }
        return value;
    }

    /**
     * Extension point for creating the MBean server.
     *
     * @return MBean server.
     * @throws Fabric3InitException If unable to initialize the MBean server.
     */
    public MBeanServer createMBeanServer() throws Fabric3InitException {
        DefaultAgent agent;
        try {
            agent = new DefaultAgent();
        } catch (ManagementException e) {
            throw new Fabric3InitException(e);
        }
        return agent.getMBeanServer();
    }

    /**
     * Extension point for creating the runtime.
     *
     * @param bootClassLoader Classloader for loading the runtime class.
     * @param configuration   the runtime configuration
     * @return Webapp runtime instance.
     * @throws Fabric3InitException If unable to initialize the runtime.
     */
    public WebappRuntime createRuntime(ClassLoader bootClassLoader, RuntimeConfiguration<WebappHostInfo> configuration) throws Fabric3InitException {
        try {
            return (WebappRuntime) bootClassLoader.loadClass(RUNTIME_CLASS).getConstructor(RuntimeConfiguration.class).newInstance(configuration);
        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException(e);
        } catch (InvocationTargetException e) {
            throw new Fabric3InitException(e);
        } catch (NoSuchMethodException e) {
            throw new Fabric3InitException(e);
        }
    }

    /**
     * Extension point for creating the monitor factory.
     *
     * @param bootClassLoader Classloader for loading the monitor factory class.
     * @return Monitor factory instance.
     * @throws Fabric3InitException If unable to initialize the monitor factory.
     */
    public MonitorFactory createMonitorFactory(ClassLoader bootClassLoader) throws Fabric3InitException {
        try {
            String monitorFactoryClass = getInitParameter(MONITOR_FACTORY_PARAM, MONITOR_FACTORY_DEFAULT);
            MonitorFactory factory = (MonitorFactory) bootClassLoader.loadClass(monitorFactoryClass).newInstance();
            URL configUrl = convertToURL(Constants.MONITOR_CONFIG_PATH, bootClassLoader);
            if (configUrl != null) {
                factory.readConfiguration(configUrl);
            }
            return factory;
        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Monitor factory Implementation not found", e);
        } catch (IOException e) {
            throw new Fabric3InitException(e);
        }
    }

    URL convertToURL(String path, ClassLoader classLoader) throws MalformedURLException {
        URL ret = null;
        if (path.charAt(0) == '/') {
            // user supplied an absolute path - look up as a webapp resource
            ret = servletContext.getResource(path);
        }
        if (ret == null) {
            // user supplied a relative path - look up as a boot classpath resource
            ret = classLoader.getResource(path);
        }
        return ret;
    }

}
