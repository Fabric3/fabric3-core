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
package org.fabric3.container.web.jetty;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.container.web.spi.InjectingSessionListener;
import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.transport.jetty.JettyService;

/**
 * Activates a web application in an embedded Jetty instance.
 *
 * @version $Rev$ $Date$
 */
public class JettyWebApplicationActivator implements WebApplicationActivator {
    private JettyService jettyService;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionResolver resolver;
    private ManagementService managementService;
    private WebApplicationActivatorMonitor monitor;
    private Map<URI, Holder> mappings;

    public JettyWebApplicationActivator(@Reference JettyService jettyService,
                                        @Reference ClassLoaderRegistry classLoaderRegistry,
                                        @Reference ContributionResolver resolver,
                                        @Reference ManagementService managementService,
                                        @Monitor WebApplicationActivatorMonitor monitor) {
        this.jettyService = jettyService;
        this.classLoaderRegistry = classLoaderRegistry;
        this.resolver = resolver;
        this.managementService = managementService;
        this.monitor = monitor;
        mappings = new ConcurrentHashMap<URI, Holder>();
    }

    @Destroy
    public void destroy() {
        for (Holder holder : mappings.values()) {
            try {
                remove(holder.getContext());
            } catch (ManagementException e) {
                monitor.error("Error removing managed bean for context: " + holder.getContext().getDisplayName(), e);
            }
        }
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        return classLoaderRegistry.getClassLoader(componentId);
    }

    @SuppressWarnings({"unchecked"})
    public ServletContext activate(String contextPath,
                                   final URI uri,
                                   URI parentClassLoaderId,
                                   final Map<String, List<Injector<?>>> injectors,
                                   ComponentContext componentContext) throws WebApplicationActivationException {
        if (mappings.containsKey(uri)) {
            throw new WebApplicationActivationException("Mapping already exists: " + uri.toString());
        }
        try {
            // resolve the url to a local artifact
            URL resolved = resolver.resolve(uri);
            ClassLoader parentClassLoader = createParentClassLoader(parentClassLoaderId, uri);
            final WebAppContext context = createWebAppContext("/" + contextPath, injectors, resolved, parentClassLoader);
            jettyService.registerHandler(context);  // the context needs to be registered before it is started

            // Use a ServletContextListener to setup session injectors and perform context injection.
            // Note context injection must be done here since servlet filters may rely on SCA reference proxies, c.f. FABRICTHREE-570
            context.addEventListener(new ServletContextListener() {
                public void contextInitialized(ServletContextEvent sce) {

                    // Setup the session listener to inject reference proxies in newly created sessions
                    // Note the listener must be added after the context is started as Jetty web xml configurer clears event listeners
                    List<Injector<HttpSession>> sessionInjectors = List.class.cast(injectors.get(SESSION_CONTEXT_SITE));
                    InjectingSessionListener listener = new InjectingSessionListener(sessionInjectors);
                    context.getSessionHandler().addEventListener(listener);
                    ServletContext servletContext = context.getServletContext();
                    try {
                        injectServletContext(servletContext, injectors);
                    } catch (ObjectCreationException e) {
                        monitor.error("Error initializing web component: " + uri, e);
                    }
                }

                public void contextDestroyed(ServletContextEvent sce) {

                }
            });
            context.start();

            ServletContext servletContext = context.getServletContext();

            Holder holder = new Holder(contextPath, context);
            mappings.put(uri, holder);
            export(context);
            monitor.activated(holder.getContextPath());
            return servletContext;
        } catch (Exception e) {
            throw new WebApplicationActivationException(e);
        }
    }

    public void deactivate(URI uri) throws WebApplicationActivationException {
        Holder holder = mappings.remove(uri);
        if (holder == null) {
            throw new WebApplicationActivationException("Mapping does not exist: " + uri.toString());
        }
        WebAppContext context = holder.getContext();
        jettyService.getServer().removeBean(context);
        try {
            remove(context);
            // Stop must called be after remove() as it releases servlets which are accessed by the latter to 
            // unregister them from the MBean server
            context.stop();
        } catch (Exception e) {
            throw new WebApplicationActivationException(e);
        }
        monitor.deactivated(holder.getContextPath());
    }

    private ClassLoader createParentClassLoader(URI parentClassLoaderId, URI id) {
        ClassLoader cl = classLoaderRegistry.getClassLoader(parentClassLoaderId);
        MultiParentClassLoader parentClassLoader = new MultiParentClassLoader(id, cl);
        // we need to make user and web container extensions available for JSP compilation
        parentClassLoader.addParent(getClass().getClassLoader());
        return parentClassLoader;
    }

    private WebAppContext createWebAppContext(String contextPath,
                                              Map<String, List<Injector<?>>> injectors,
                                              URL resolved, ClassLoader parentClassLoader) throws IOException {
        WebAppContext context = new ManagedWebAppContext(resolved.toExternalForm(), contextPath);
        context.setParentLoaderPriority(true);
        InjectingDecorator decorator = new InjectingDecorator(injectors);
        context.addDecorator(decorator);
        WebAppClassLoader webAppClassLoader;
        webAppClassLoader = new WebAppClassLoader(parentClassLoader, context);
        context.setClassLoader(webAppClassLoader);
        context.setHandler(new WorkContextHandler());
        return context;
    }

    @SuppressWarnings({"unchecked"})
    private void injectServletContext(ServletContext servletContext, Map<String, List<Injector<?>>> injectors) throws ObjectCreationException {
        List<Injector<?>> list = injectors.get(SERVLET_CONTEXT_SITE);
        if (list == null) {
            // nothing to inject
            return;
        }
        for (Injector injector : list) {
            injector.inject(servletContext);
        }
    }

    private void export(WebAppContext context) throws ManagementException {
        String webAppName = encodeName(context.getDisplayName());
        managementService.export(webAppName, "webapps/" + webAppName, "web application", context);
        ServletHandler handler = context.getServletHandler();
        for (ServletHolder servletHolder : handler.getServlets()) {
            final String group = "webapps/" + webAppName + "/servlets";
            managementService.export(webAppName + "/" + servletHolder.getName(), group, "web application", servletHolder);
        }
    }

    private void remove(WebAppContext context) throws ManagementException {
        String webAppName = encodeName(context.getDisplayName());
        managementService.remove(webAppName, "webapps/" + webAppName);
        ServletHandler handler = context.getServletHandler();
        for (ServletHolder servletHolder : handler.getServlets()) {
            final String group = "webapps/" + webAppName + "/servlets";
            managementService.remove(webAppName + "/" + servletHolder.getName(), group);
        }
    }

    private String encodeName(String name) {
        return name.toLowerCase().replace('\n', ' ');
    }


    private static class Holder {
        private String contextPath;
        private WebAppContext context;

        private Holder(String contextPath, WebAppContext context) {
            this.contextPath = contextPath;
            this.context = context;
        }

        public String getContextPath() {
            return contextPath;
        }

        public WebAppContext getContext() {
            return context;
        }
    }

}
