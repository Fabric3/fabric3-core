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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private boolean reload = false;
    private List<Resource> resources;

    public RsContainer() {
        this.resources = new ArrayList<Resource>();
        reload = true;
    }

    public void addResource(Resource resource) {
        resources.add(resource);
        reload = true;
    }

    public void init(ServletConfig config) {
        servletConfig = config;
    }

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        reload();

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

    private void reload() throws ServletException {
        if (!reload) {
            return;
        }
        try {
            // register contribution resources
            ResourceConfig resourceConfig = new ResourceConfig();
            for (Resource resource : resources) {
                resourceConfig.registerResources(resource);
            }
            servlet = new ServletContainer(resourceConfig);
            servlet.init(servletConfig);
        } catch (ServletException se) {
            se.printStackTrace();
            throw se;
        } catch (Throwable t) {
            ServletException se = new ServletException(t);
            se.printStackTrace();
            throw se;
        }
        reload = false;
    }

}
