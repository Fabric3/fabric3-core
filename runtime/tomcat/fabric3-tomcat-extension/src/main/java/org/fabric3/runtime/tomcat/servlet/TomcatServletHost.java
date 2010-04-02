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
package org.fabric3.runtime.tomcat.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.host.ServletHost;

/**
 * Implementation of ServletHost that bridges to the host Tomcat runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class TomcatServletHost implements ServletHost {
    private int defaultHttpPort = 8080;   // default Tomcat port
    private String servicePath = "";      // context path for bound services; defaults to the root context
    private int defaultHttpsPort = -1;
    private Service service;
    private Connector defaultHttpConnector;
    private Fabric3DispatchingServlet dispatchingServlet;


    public TomcatServletHost(@Reference Service service) {
        this.service = service;
    }

    @Property(required = false)
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    @Init
    public void init() throws ServletHostException {
        for (Connector connector : service.findConnectors()) {
            if (connector.getPort() == defaultHttpPort) {
                defaultHttpConnector = connector;
                break;
            }
        }
        if (defaultHttpConnector == null) {
            throw new ServletHostException("Default HTTP connector not found for port: " + defaultHttpPort
                    + ". Ensure that the Fabric3 runtime HTTP port is configured in systemConfig.xml.");
        }
        dispatchingServlet = new Fabric3DispatchingServlet();
        Fabric3ServletWrapper wrapper = new Fabric3ServletWrapper(dispatchingServlet);
        wrapper.setName("Fabric3Servlet");
        for (Container container : defaultHttpConnector.getContainer().findChildren()) {
            if (container instanceof StandardHost) {
                Container child = container.findChild("");
                if (child != null) {
                	   container.removeChild(child);
                }
                StandardContext context = createContext("", ".");
                context.addChild(wrapper);
                context.addServletMapping("/*", "Fabric3Servlet");
                container.addChild(context);

                try {
                    dispatchingServlet.init(wrapper);
                } catch (ServletException e) {
                    throw new AssertionError(e);
                }
            }
        }

    }

    public int getHttpPort() {
        return defaultHttpPort;
    }

    public int getHttpsPort() {
        return defaultHttpsPort;
    }

    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public boolean isHttpsEnabled() {
        return defaultHttpsPort != -1;
    }

    public void registerMapping(String mapping, Servlet servlet) {
        try {
            dispatchingServlet.registerMapping(mapping, servlet);
        } catch (ServletException e) {
            // FIXME
            throw new AssertionError(e);
        }

    }

    public Servlet unregisterMapping(String mapping) {
        throw new UnsupportedOperationException();
    }

    public boolean isMappingRegistered(String mapping) {
        return false;
    }

    private StandardContext createContext(String path, String docBase) {
        StandardContext context = new StandardContext();
        context.setUnpackWAR(false);
        context.setDocBase(docBase);
        context.setPath(path);
        ContextConfig config = new ContextConfig();
        context.addLifecycleListener(config);
        return (context);

    }
}
