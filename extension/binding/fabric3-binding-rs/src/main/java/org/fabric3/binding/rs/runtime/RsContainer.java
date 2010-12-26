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
package org.fabric3.binding.rs.runtime;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Manages resources defined in a deployable contribution.
 *
 * @version $Rev$ $Date$
 */
public final class RsContainer extends HttpServlet {
    private static final long serialVersionUID = 1954697059021782141L;

    private ClassLoader classLoader;
    private Fabric3ProviderFactory providerFactory;

    private RsServlet servlet;
    private ServletConfig servletConfig;
    private boolean reload = false;
    private ReentrantReadWriteLock reloadRWLock = new ReentrantReadWriteLock();
    private Lock reloadLock = reloadRWLock.readLock();
    private Lock serviceLock = reloadRWLock.writeLock();

    public RsContainer(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.providerFactory = new Fabric3ProviderFactory();
        reload = true;
    }

    public void addResource(Class<?> resource, Object instance) {
        providerFactory.addResource(resource, instance);
        reload = true;
    }

    @Override
    public void init(ServletConfig config) {
        servletConfig = new ServletConfigWrapper(config);
    }

    public void reload() throws ServletException {
        try {
            reloadLock.lock();
            // Set the class loader to the runtime one so Jersey loads the
            // ResourceConfig properly
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                servlet = new RsServlet(this.providerFactory);
                servlet.init(servletConfig);
            } catch (ServletException se) {
                se.printStackTrace();
                throw se;
            } catch (Throwable t) {
                ServletException se = new ServletException(t);
                se.printStackTrace();
                throw se;
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
            reload = false;
        } finally {
            reloadLock.unlock();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            serviceLock.lock();
            if (reload) {
                reload();
            }

            ClassLoader old = Thread.currentThread().getContextClassLoader();
            WorkContext oldContext = null;
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                WorkContext workContext = new WorkContext();
                workContext.setHeader("fabric3.httpRequest", req);
                workContext.setHeader("fabric3.httpResponse", res);
                CallFrame frame = new CallFrame();
                workContext.addCallFrame(frame);
                oldContext = WorkContextTunnel.setThreadWorkContext(workContext);
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
                WorkContextTunnel.setThreadWorkContext(oldContext);
            }
        } finally {
            serviceLock.unlock();
        }
    }

    /**
     * Wrapper class to add the Jersey resource class as a web app init parameter
     */
    public class ServletConfigWrapper implements ServletConfig {
        private ServletConfig servletConfig;

        public ServletConfigWrapper(ServletConfig servletConfig) {
            this.servletConfig = servletConfig;
        }

        public String getInitParameter(String name) {
            if ("javax.ws.rs.Application".equals(name)) {
                return Fabric3ResourceConfig.class.getName();
            }
            return servletConfig.getInitParameter(name);
        }

        public Enumeration<String> getInitParameterNames() {
            final Enumeration<String> e = servletConfig.getInitParameterNames();
            return new Enumeration<String>() {
                boolean finished = false;

                public boolean hasMoreElements() {
                    return e.hasMoreElements() || !finished;
                }

                public String nextElement() {
                    if (e.hasMoreElements()) {
                        return e.nextElement();
                    }
                    if (!finished) {
                        finished = true;
                        return "javax.ws.rs.Application";
                    }
                    return null;
                }
            };
        }

        public ServletContext getServletContext() {
            return servletConfig.getServletContext();
        }

        public String getServletName() {
            return servletConfig.getServletName();
        }
    }
}
