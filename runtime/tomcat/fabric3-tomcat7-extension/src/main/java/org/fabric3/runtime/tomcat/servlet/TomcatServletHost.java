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
package org.fabric3.runtime.tomcat.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.catalina.Container;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;
import org.fabric3.runtime.tomcat.connector.ConnectorService;
import org.fabric3.spi.host.ServletHost;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation of ServletHost that bridges to the host Tomcat runtime.
 */
@EagerInit
public class TomcatServletHost implements ServletHost {
    private ConnectorService connectorService;
    private int defaultHttpPort = 8080;   // default Tomcat port
    private String servicePath = "";      // context path for bound services; defaults to the root context
    private int defaultHttpsPort = -1;
    private Fabric3DispatchingServlet dispatchingServlet;

    public TomcatServletHost(@Reference ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    @Property(required = false)
    public void setHttpPort(int defaultHttpPort) {
        this.defaultHttpPort = defaultHttpPort;
    }

    @Property(required = false)
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    @Init
    public void init() throws ServletHostException {
        Connector connector = connectorService.getConnector();
        dispatchingServlet = new Fabric3DispatchingServlet();
        Fabric3ServletWrapper wrapper = new Fabric3ServletWrapper(dispatchingServlet);
        wrapper.setName("Fabric3Servlet");
        for (Container container : connector.getService().getContainer().findChildren()) {
            if (container instanceof StandardHost) {
                Container child = container.findChild("");
                if (child != null) {
                    container.removeChild(child);
                }
                StandardContext context = createContext(servicePath, ".");
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

    public String getHostType() {
        return "Tomcat";
    }

    public int getHttpPort() {
        return defaultHttpPort;
    }

    public int getHttpsPort() {
        return defaultHttpsPort;
    }

    public boolean isHttpsEnabled() {
        return defaultHttpsPort != -1;
    }

    public URL getBaseHttpUrl() {
        // TODO support returning a host bound to a different address
        if (connectorService != null) {
            try {
                String host = InetAddress.getLocalHost().getHostAddress();
                return new URL("http://" + host + ":" + getHttpPort());
            } catch (UnknownHostException | MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    public URL getBaseHttpsUrl() {
        // TODO support returning a host bound to a different address
        if (connectorService != null) {
            try {
                String host = InetAddress.getLocalHost().getHostAddress();
                return new URL("https://" + host + ":" + getHttpPort());
            } catch (UnknownHostException | MalformedURLException e) {
                throw new IllegalStateException(e);
            }

        }
        return null;
    }

    public void registerMapping(String mapping, Servlet servlet) {
        try {
            dispatchingServlet.registerMapping(mapping, servlet);
        } catch (ServletException e) {
            throw new AssertionError(e);
        }

    }

    public Servlet unregisterMapping(String mapping) {
        try {
            return dispatchingServlet.unregisterMapping(mapping);
        } catch (ServletException e) {
            throw new AssertionError(e);
        }
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
