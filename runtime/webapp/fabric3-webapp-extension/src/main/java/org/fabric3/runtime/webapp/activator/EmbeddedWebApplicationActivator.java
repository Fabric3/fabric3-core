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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.webapp.activator;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * A WebApplicationActivator used in a runtime embedded in a WAR.
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {WebApplicationActivator.class, HttpSessionListener.class})
public class EmbeddedWebApplicationActivator implements WebApplicationActivator, HttpSessionListener {
    private ServletHost host;
    private List<Injector<HttpSession>> sessionInjectors;

    public EmbeddedWebApplicationActivator(@Reference ServletHost host) {
        this.host = host;
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        //  As the runtime is embedded in a web app, the TCCL is the webapp classloader
        return Thread.currentThread().getContextClassLoader();
    }

    @SuppressWarnings({"unchecked"})
    public ServletContext activate(String contextPath,
                                   URI uri,
                                   URI parentClassLoaderId,
                                   Map<String, List<Injector<?>>> injectors,
                                   ComponentContext context) throws WebApplicationActivationException {
        try {
            // the web app has already been activated since it is embedded in a war. Just inject references and properties
            ServletContext servletContext = host.getServletContext();
            injectServletContext(servletContext, injectors);
            sessionInjectors = List.class.cast(injectors.get(SESSION_CONTEXT_SITE));
            return servletContext;
        } catch (ObjectCreationException e) {
            throw new WebApplicationActivationException(e);
        }
    }

    public void deactivate(URI uri) throws WebApplicationActivationException {
        // do nothing
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

    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        // inject conversational proxies into the session
        for (Injector<HttpSession> injector : sessionInjectors) {
            try {
                injector.inject(session);
            } catch (ObjectCreationException e) {
                throw new RuntimeInjectionException(e);
            }
        }
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        // do nothing
    }
}
