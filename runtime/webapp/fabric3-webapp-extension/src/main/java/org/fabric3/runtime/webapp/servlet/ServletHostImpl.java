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
package org.fabric3.runtime.webapp.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.runtime.webapp.ServletRequestInjector;
import org.fabric3.runtime.webapp.WebappHostInfo;
import org.fabric3.spi.host.ServletHost;

/**
 * A <code>ServletHost</code> implementation that forwards requests to registered servlets
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {ServletHost.class, ServletRequestInjector.class})
@EagerInit
public class ServletHostImpl implements ServletHost, ServletRequestInjector {
    private Map<String, Servlet> servlets;
    private WebappHostInfo info;

    public ServletHostImpl(@Reference WebappHostInfo info) {
        this.info = info;
        servlets = new ConcurrentHashMap<String, Servlet>();
    }

    public int getHttpPort() {
        return -1;
    }

    public int getHttpsPort() {
        return -1;
    }

    public boolean isHttpsEnabled() {
        return false;
    }

    public ServletContext getServletContext() {
        return info.getServletContext();
    }

    public void init(ServletConfig config) throws ServletException {
        for (Servlet servlet : servlets.values()) {
            servlet.init(config);
        }
    }

    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        assert req instanceof HttpServletRequest;
        String path = ((HttpServletRequest) req).getPathInfo();
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