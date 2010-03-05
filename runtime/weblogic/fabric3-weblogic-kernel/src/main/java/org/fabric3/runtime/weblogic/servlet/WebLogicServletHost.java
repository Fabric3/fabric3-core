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
*/
package org.fabric3.runtime.weblogic.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.runtime.weblogic.api.Constants;
import org.fabric3.runtime.weblogic.api.ServletRequestDispatcher;
import org.fabric3.spi.host.ServletHost;

/**
 * A <code>ServletHost</code> implementation that forwards requests to registered servlets
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {ServletHost.class, ServletRequestDispatcher.class})
@EagerInit
public class WebLogicServletHost extends HttpServlet implements ServletHost, ServletRequestDispatcher {
    private static final long serialVersionUID = -3784698338450289318L;
    private MBeanServer mBeanServer;
    private Map<String, Servlet> servlets = new ConcurrentHashMap<String, Servlet>();
    private int httpPort;
    private int httpsPort;

    public WebLogicServletHost(@Reference MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Init
    public void start() throws JMException, ServletHostInitException, MalformedURLException {
        // determine the default HTTP and HTTPS URLs 
        ObjectName serverRuntimeMBean = (ObjectName) mBeanServer.getAttribute(Constants.WLS_RUNTIME_SERVICE_MBEAN, "ServerRuntime");
        String httpUrl = (String) mBeanServer.invoke(serverRuntimeMBean, "getURL", new Object[]{"http"}, new String[]{String.class.getName()});
        if (httpUrl == null) {
            throw new ServletHostInitException("HTTP port not configured");
        }
        httpPort = new URL(httpUrl).getPort();

        String httpsUrl = (String) mBeanServer.invoke(serverRuntimeMBean, "getURL", new Object[]{"https"}, new String[]{String.class.getName()});
        if (httpsUrl == null) {
            return;
        }
        httpsPort = new URL(httpsUrl).getPort();

    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public boolean isHttpsEnabled() {
        return true;
    }

    public void init(ServletConfig config) throws ServletException {
        for (Servlet servlet : servlets.values()) {
            servlet.init(config);
        }
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            throw new IllegalStateException("No servlet registered for empty path");
        }
        Servlet servlet = servlets.get(path);
        if (servlet == null) {
            int i;
            servlet = servlets.get(path + "/*");
            if (servlet == null) {
                while ((i = path.lastIndexOf("/")) >= 0) {
                    servlet = servlets.get(path.substring(0, i) + "/*");
                    if (servlet != null) {
                        break;
                    }
                    path = path.substring(0, i);
                }
            }
            if (servlet == null) {
                throw new IllegalStateException("No servlet registered for path: " + path);
            }
        }
        servlet.service(req, resp);
    }

    public void registerMapping(String path, Servlet servlet) {
        if (servlets.containsKey(path)) {
            throw new IllegalStateException("Servlet already registered at path: " + path);
        }
        servlets.put(path, servlet);
    }

    public boolean isMappingRegistered(String mapping) {
        return servlets.containsKey(mapping);

    }

    public Servlet unregisterMapping(String path) {
        return servlets.remove(path);
    }

}