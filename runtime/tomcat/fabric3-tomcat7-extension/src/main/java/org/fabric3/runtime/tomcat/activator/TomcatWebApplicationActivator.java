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
package org.fabric3.runtime.tomcat.activator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.Container;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.InstanceManager;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.container.web.spi.InjectingSessionListener;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.runtime.tomcat.connector.ConnectorService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.contribution.ContributionResolver;
import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Activates a web component in the host Tomcat runtime.
 */
public class TomcatWebApplicationActivator implements WebApplicationActivator {
    private ConnectorService connectorService;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionResolver resolver;
    private Connector connector;
    // mappings from component URI to Tomcat context path
    private Map<URI, String> mappings = new ConcurrentHashMap<>();
    private ActivatorMonitor monitor;

    public TomcatWebApplicationActivator(
            @Reference ConnectorService connectorService,
            @Reference ClassLoaderRegistry registry,
            @Reference ContributionResolver resolver,
            @Monitor ActivatorMonitor monitor) {
        this.connectorService = connectorService;
        this.classLoaderRegistry = registry;
        this.resolver = resolver;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        connector = connectorService.getConnector();
    }

    @Destroy
    public void cleanWebApps() {
        Set<URI> webApps = mappings.keySet();
        for (URI webApp : webApps) {
            try {
                deactivate(webApp);
            } catch (Fabric3Exception e) {
                monitor.error(webApp.toString(), e);
            }
        }
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        return classLoaderRegistry.getClassLoader(componentId);
    }

    @SuppressWarnings({"unchecked"})
    public ServletContext activate(String contextPath,
                                   URI uri,
                                   URI parentClassLoaderId,
                                   Map<String, List<Injector<?>>> injectors,
                                   ComponentContext componentContext) throws Fabric3Exception {
        if (mappings.containsKey(uri)) {
            throw new Fabric3Exception("Mapping already exists: " + uri.toString());
        }
        contextPath = "/" + contextPath;
        try {
            // resolve the url to a local artifact
            URL resolved = resolver.resolve(uri);
            ClassLoader parentClassLoader = createParentClassLoader(parentClassLoaderId, uri);
            StandardContext context = createContext(contextPath, resolved.getFile(), parentClassLoader, injectors);
            for (Container container : connector.getService().getContainer().findChildren()) {
                if (container instanceof StandardHost) {
                    container.addChild(context);
                }
            }
            // Setup the session listener to inject reference proxies in newly created sessions.
            // Note this must be performed after the context is added as a child to StandardHost as doing so starts the context which results in the
            // application lifecycle listeners being reset.
            List<Injector<HttpSession>> sessionInjectors = List.class.cast(injectors.get(SESSION_CONTEXT_SITE));
            InjectingSessionListener listener = new InjectingSessionListener(sessionInjectors);
            Object[] listeners = context.getApplicationLifecycleListeners();
            Object[] newListeners = new Object[listeners.length + 1];
            System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
            newListeners[listeners.length] = listener;
            context.setApplicationLifecycleListeners(newListeners);

            ServletContext servletContext = context.getServletContext();
            // make references available in the servlet context
            injectServletContext(servletContext, injectors);

            mappings.put(uri, contextPath);
            monitor.activated(contextPath);
            return servletContext;
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }

    }

    public void deactivate(URI uri) throws Fabric3Exception {
        String contextPath = mappings.remove(uri);
        if (contextPath == null) {
            throw new Fabric3Exception("Context not registered for component: " + uri);
        }
        for (Container container : connector.getService().getContainer().findChildren()) {
            if (container instanceof StandardHost) {
                StandardContext context = (StandardContext) container.findChild(contextPath);
                container.removeChild(context);
                monitor.deactivated(contextPath);
                break;
            }
        }
    }

    private ClassLoader createParentClassLoader(URI parentClassLoaderId, URI id) {
        ClassLoader cl = classLoaderRegistry.getClassLoader(parentClassLoaderId);
        return new MultiParentClassLoader(id, cl);
    }

    private StandardContext createContext(String path, String docBase, ClassLoader classLoader, Map<String, List<Injector<?>>> injectors) {
        StandardContext context = new StandardContext();
        ContextConfig config = new ContextConfig();
        context.addLifecycleListener(config);
        context.setParentClassLoader(classLoader);

        for (Container container : connector.getService().getContainer().findChildren()) {
            if (container instanceof StandardHost) {
                context.setParent(container);
            }
        }

        context.setPath(path);
        context.setDocBase(docBase);

        Fabric3Loader loader = new Fabric3Loader(classLoader);
        context.setLoader(loader);
        // turn off unpacking so Tomcat does not copy the extracted WAR to the webapps directory and autodeploy it on a subsequent boot
        context.setUnpackWAR(false);
        Fabric3InstanceManager instanceManager = new Fabric3InstanceManager(injectors, classLoader);
        context.setInstanceManager(instanceManager);
        context.getServletContext().setAttribute(InstanceManager.class.getName(), instanceManager);
        return (context);
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

}
