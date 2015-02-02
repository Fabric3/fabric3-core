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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.container.web.jetty;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.container.web.spi.InjectingSessionListener;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.injection.Injector;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.transport.jetty.JettyService;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Reference;

/**
 * Activates a web application in an embedded Jetty instance.
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
        mappings = new ConcurrentHashMap<>();
    }

    @Destroy
    public void destroy() {
        for (Holder holder : mappings.values()) {
            try {
                remove(holder.getContext());
            } catch (Fabric3Exception e) {
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
                                   ComponentContext componentContext) throws Fabric3Exception {
        if (mappings.containsKey(uri)) {
            throw new Fabric3Exception("Mapping already exists: " + uri.toString());
        }
        try {
            // resolve the url to a local artifact
            List<URL> locations = resolver.resolveAllLocations(uri);
            ClassLoader parentClassLoader = createParentClassLoader(parentClassLoaderId, uri);
            final WebAppContext context = createWebAppContext("/" + contextPath, injectors, locations, parentClassLoader);

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
                    } catch (Fabric3Exception e) {
                        monitor.error("Error initializing web component: " + uri, e);
                    }
                }

                // remove all the registered Listeners
                // Note the listeners must also be unregistered , c.f. FABRICTHREE-630
                public void contextDestroyed(ServletContextEvent sce) {
                    context.getSessionHandler().clearEventListeners();
                }
            });
            jettyService.registerHandler(context);  // the context needs to be registered before it is started
            context.start();

            ServletContext servletContext = context.getServletContext();

            Holder holder = new Holder(contextPath, context);
            mappings.put(uri, holder);
            export(context);
            monitor.activated(holder.getContextPath());
            return servletContext;
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

    public void deactivate(URI uri) throws Fabric3Exception {
        Holder holder = mappings.remove(uri);
        if (holder == null) {
            throw new Fabric3Exception("Mapping does not exist: " + uri.toString());
        }
        WebAppContext context = holder.getContext();
        jettyService.getServer().removeBean(context);
        try {
            remove(context);
            // Stop must called be after remove() as it releases servlets which are accessed by the latter to 
            // unregister them from the MBean server
            context.stop();
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
        context.setClassLoader(null);
        jettyService.removeHandler(context);

        monitor.deactivated(holder.getContextPath());
    }

    private ClassLoader createParentClassLoader(URI parentClassLoaderId, URI id) {
        ClassLoader cl = classLoaderRegistry.getClassLoader(parentClassLoaderId);
        MultiParentClassLoader parentClassLoader = new MultiParentClassLoader(id, cl);
        // we need to make user and web container extensions available for JSP compilation
        parentClassLoader.addParent(getClass().getClassLoader());
        return parentClassLoader;
    }

    private WebAppContext createWebAppContext(String contextPath, Map<String, List<Injector<?>>> injectors, List<URL> locations, ClassLoader parentClassLoader)
            throws IOException {

        WebAppContext context;

        if (locations.size() == 1) {
            context = new ManagedWebAppContext(locations.get(0).toExternalForm(), contextPath);
        } else {
            context = new ManagedWebAppContext(null, contextPath);
            // add the resource paths
            String[] paths = new String[locations.size()];
            for (int i = 0; i < locations.size(); i++) {
                URL location = locations.get(i);
                paths[i] = (location.toExternalForm());
            }
            ResourceCollection resources = new ResourceCollection(paths);
            context.setBaseResource(resources);
        }

        context.setParentLoaderPriority(true);
        InjectingDecorator decorator = new InjectingDecorator(injectors);
        context.addDecorator(decorator);
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(parentClassLoader, context);
        context.setClassLoader(webAppClassLoader);

        // don't extract the war since this has already been done by the WAR classpath processor
        context.setExtractWAR(false);
        Configuration[] configurations = createConfigurations();
        context.setConfigurations(configurations);
        return context;
    }

    @SuppressWarnings({"unchecked"})
    private void injectServletContext(ServletContext servletContext, Map<String, List<Injector<?>>> injectors) throws Fabric3Exception {
        List<Injector<?>> list = injectors.get(SERVLET_CONTEXT_SITE);
        if (list == null) {
            // nothing to inject
            return;
        }
        for (Injector injector : list) {
            injector.inject(servletContext);
        }
    }

    private void export(WebAppContext context) throws Fabric3Exception {
        String displayName = context.getDisplayName();
        if (displayName == null) {
            displayName = UUID.randomUUID().toString();
        }
        String webAppName = encodeName(displayName);
        managementService.export(webAppName, "webapps/" + webAppName, "web application", context);
        ServletHandler handler = context.getServletHandler();
        for (ServletHolder servletHolder : handler.getServlets()) {
            final String group = "webapps/" + webAppName + "/servlets";
            managementService.export(webAppName + "/" + servletHolder.getName(), group, "web application", servletHolder);
        }
    }

    private void remove(WebAppContext context) throws Fabric3Exception {
        String displayName = context.getDisplayName();
        if (displayName == null) {
            displayName = context.toString();
        }
        String webAppName = encodeName(displayName);
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

    /**
     * Creates Jetty configurations, overriding the default WebInfConfiguration to not scan library resources and create duplicates (the resources are already
     * on the classpath).
     *
     * @return the configuration
     */
    private Configuration[] createConfigurations() {
        WebInfConfiguration webInfConfig = new NonScanningWebInfConfiguration();
        WebXmlConfiguration webXmlConfig = new WebXmlConfiguration();
        MetaInfConfiguration metaInfConfig = new MetaInfConfiguration();
        FragmentConfiguration fragmentConfig = new FragmentConfiguration();
        JettyWebXmlConfiguration jettyXmlConfig = new JettyWebXmlConfiguration();
        AnnotationConfiguration annotationConfig = new AnnotationConfiguration();

        return new Configuration[]{webInfConfig, webXmlConfig, metaInfConfig, fragmentConfig, jettyXmlConfig, annotationConfig};
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
