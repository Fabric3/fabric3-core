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
package org.fabric3.runtime.tomcat.activator;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.AnnotationProcessor;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.container.web.spi.InjectingSessionListener;
import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.runtime.tomcat.connector.ConnectorService;
import org.fabric3.runtime.tomcat.servlet.ServletHostException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * Activates a web component in the host Tomcat runtime.
 *
 * @version $Rev$ $Date$
 */
public class TomcatWebApplicationActivator implements WebApplicationActivator {
    private ConnectorService connectorService;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionResolver resolver;
    private Connector connector;
    // mappings from component URI to Tomcat context path
    private Map<URI, String> mappings = new ConcurrentHashMap<URI, String>();
    private ActivatorMonitor monitor;

    public TomcatWebApplicationActivator(@Reference ConnectorService connectorService,
                                         @Reference ClassLoaderRegistry registry,
                                         @Reference ContributionResolver resolver,
                                         @Monitor ActivatorMonitor monitor) {
        this.connectorService = connectorService;
        this.classLoaderRegistry = registry;
        this.resolver = resolver;
        this.monitor = monitor;
    }

    @Init
    public void init() throws ServletHostException {
        connector = connectorService.getConnector();
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        return classLoaderRegistry.getClassLoader(componentId);
    }

    public ServletContext activate(String contextPath,
                                   URI uri,
                                   URI parentClassLoaderId,
                                   Map<String, List<Injector<?>>> injectors,
                                   ComponentContext componentContext) throws WebApplicationActivationException {
        if (mappings.containsKey(uri)) {
            throw new WebApplicationActivationException("Mapping already exists: " + uri.toString());
        }
        contextPath = "/" + contextPath;
        try {
            // resolve the url to a local artifact
            URL resolved = resolver.resolve(uri);
            ClassLoader parentClassLoader = createParentClassLoader(parentClassLoaderId, uri);
            StandardContext context = createContext(contextPath, resolved.getFile(), parentClassLoader, injectors);
            for (Container container : connector.getContainer().findChildren()) {
                if (container instanceof StandardHost) {
                    container.addChild(context);
                }
            }
            // Setup the session listener to inject conversational reference proxies in newly created sessions.
            // Note this must be performed after the context is added as a child to StandardHost as doing so
            // starts the context which results in the application lifecycle listeners being reset.
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
            throw new WebApplicationActivationException(e);
        }

    }

    public void deactivate(URI uri) throws WebApplicationActivationException {
        String contextPath = mappings.remove(uri);
        if (contextPath == null) {
            throw new WebApplicationActivationException("Context not registered for component: " + uri);
        }
        StandardContext context = null;
        try {
            for (Container container : connector.getContainer().findChildren()) {
                if (container instanceof StandardHost) {
                    context = (StandardContext) container.findChild(contextPath);
                    container.removeChild(context);
                    try {
                        context.destroy();
                    } catch (Exception e) {
                        throw new WebApplicationActivationException(e);
                    }
                    break;
                }
            }
            if (context == null) {
                throw new WebApplicationActivationException("Context not found for: " + contextPath);
            }
            context.stop();
            monitor.deactivated(contextPath);
        } catch (LifecycleException e) {
            throw new WebApplicationActivationException(e);
        }
    }

    private ClassLoader createParentClassLoader(URI parentClassLoaderId, URI id) {
        ClassLoader cl = classLoaderRegistry.getClassLoader(parentClassLoaderId);
        return new MultiParentClassLoader(id, cl);
    }

    private StandardContext createContext(String path, String docBase, ClassLoader classLoader, Map<String, List<Injector<?>>> injectors) {
        StandardContext context = new StandardContext();
        context.setDocBase(docBase);
        context.setPath(path);
        ContextConfig config = new ContextConfig();
        context.addLifecycleListener(config);
        context.setParentClassLoader(classLoader);
        Fabric3Loader loader = new Fabric3Loader(classLoader);
        context.setLoader(loader);
        // turn off unpacking so Tomcat does not copy the extracted WAR to the /webapps directory and autodeploy it on a subsequent boot
        context.setUnpackWAR(false);
        Fabric3AnnotationProcessor annotationProcessor = new Fabric3AnnotationProcessor(injectors);
        context.setAnnotationProcessor(annotationProcessor);
        context.getServletContext().setAttribute(AnnotationProcessor.class.getName(), annotationProcessor);
        return (context);
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

}
