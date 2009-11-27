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
package org.fabric3.container.web.jetty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.container.web.spi.InjectingSessionListener;
import org.fabric3.transport.jetty.JettyService;
import org.fabric3.spi.Injector;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.ContributionUriResolver;

/**
 * Activates a web application in an embedded Jetty instance.
 *
 * @version $Rev$ $Date$
 */
public class JettyWebApplicationActivator implements WebApplicationActivator {
    private JettyService jettyService;
    private ClassLoaderRegistry classLoaderRegistry;
    private WebApplicationActivatorMonitor monitor;
    private Map<URI, Holder> mappings;
    private Map<String, ContributionUriResolver> resolvers;

    public JettyWebApplicationActivator(@Reference JettyService jettyService,
                                        @Reference ClassLoaderRegistry classLoaderRegistry,
                                        @Monitor WebApplicationActivatorMonitor monitor) {
        this.jettyService = jettyService;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
        mappings = new ConcurrentHashMap<URI, Holder>();
    }

    /**
     * Lazily injects the contribution URI resolvers that may be supplied by extensions.
     *
     * @param resolvers the resolvers keyed by URI scheme
     */
    @Reference
    public void setContributionUriResolver(Map<String, ContributionUriResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        return classLoaderRegistry.getClassLoader(componentId);
    }

    @SuppressWarnings({"unchecked"})
    public ServletContext activate(String contextPath,
                                   URI uri,
                                   URI parentClassLoaderId,
                                   Map<String, List<Injector<?>>> injectors,
                                   ComponentContext componentContext) throws WebApplicationActivationException {
        if (mappings.containsKey(uri)) {
            throw new WebApplicationActivationException("Mapping already exists: " + uri.toString());
        }
        ContributionUriResolver resolver = getResolver(uri);
        try {
            // resolve the url to a local artifact
            URL resolved = resolver.resolve(uri);
            ClassLoader parentClassLoader = createParentClassLoader(parentClassLoaderId, uri);
            WebAppContext context = createWebAppContext("/" + contextPath, injectors, resolved, parentClassLoader);
            jettyService.registerHandler(context);  // the context needs to be registered before it is started
            context.start();
            // Setup the session listener to inject conversational reference proxies in newly created sessions
            // Note the listener must be added after the context is started as Jetty web xml configurer clears event listeners
            List<Injector<HttpSession>> sessionInjectors = List.class.cast(injectors.get(SESSION_CONTEXT_SITE));
            InjectingSessionListener listener = new InjectingSessionListener(sessionInjectors);
            context.getSessionHandler().addEventListener(listener);
            ServletContext servletContext = context.getServletContext();
            injectServletContext(servletContext, injectors);
            Holder holder = new Holder(contextPath, context);
            mappings.put(uri, holder);
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
        jettyService.getServer().removeLifeCycle(context);
        try {
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
                                              URL resolved, ClassLoader parentClassLoader) throws IOException, URISyntaxException {
        WebAppContext context = new WebAppContext(resolved.toExternalForm(), contextPath);
        context.setParentLoaderPriority(true);

        context.setServletHandler(new InjectingServletHandler(injectors));
        WebAppClassLoader webAppClassLoader;
        webAppClassLoader = new WebAppClassLoader(parentClassLoader, context);
        context.setClassLoader(webAppClassLoader);
        context.addHandler(new WorkContextHandler());
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

    private ContributionUriResolver getResolver(URI uri) throws WebApplicationActivationException {
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = ContributionUriResolver.LOCAL_SCHEME;
        }
        ContributionUriResolver resolver = resolvers.get(scheme);
        if (resolver == null) {
            throw new WebApplicationActivationException("Contribution resolver for scheme not found: " + scheme);
        }
        return resolver;
    }

}
