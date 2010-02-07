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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.webapp;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.xml.namespace.QName;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.host.Names;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;

/**
 * Bootstrapper for the Fabric3 runtime in a web application host. This listener manages one runtime per servlet context; the lifecycle of that
 * runtime corresponds to the the lifecycle of the associated servlet context.
 * <p/>
 * The bootstrapper launches the runtime, booting system extensions and applications, according to the servlet parameters defined in {@link
 * Constants}. When the runtime is instantiated, it is placed in the servlet context with the attribute {@link Constants#RUNTIME_ATTRIBUTE}. The
 * runtime implements {@link WebappRuntime} so that filters and servlets loaded in the parent web app classloader may pass events and requests to it.
 * <p/>
 *
 * @version $$Rev$$ $$Date$$
 */

public class WebappRuntimeImpl extends AbstractRuntime<WebappHostInfo> implements WebappRuntime {
    private ServletRequestInjector requestInjector;
    private HttpSessionListener sessionListener;
    private F3RequestListener listener;

    public WebappRuntimeImpl() {
        super(WebappHostInfo.class);
    }

    public void deploy(QName qName, URI componentId) throws ContributionException, DeploymentException {
        try {
            // contribute the war to the application domain
            Domain domain = getComponent(Domain.class, APPLICATION_DOMAIN_URI);
            listener = getComponent(F3RequestListener.class, F3RequestListener.LISTENER_URI);
            ContributionService contributionService = getComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            URI contributionUri = new URI("file", qName.getLocalPart(), null);
            WarContributionSource source = new WarContributionSource(contributionUri);
            contributionService.contribute(source);
            // activate the deployable composite in the domain
            domain.include(qName);
        } catch (MalformedURLException e) {
            throw new DeploymentException("Invalid web archive", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Error processing project", e);
        }
    }

    public ServletRequestInjector getRequestInjector() {
        if (requestInjector == null) {
            URI uri = URI.create(Names.RUNTIME_NAME + "/servletHost");
            requestInjector = getComponent(ServletRequestInjector.class, uri);
        }
        return requestInjector;
    }

    public void requestInitialized(ServletRequestEvent sre) {
        listener.onRequestStart(sre);
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        listener.onRequestEnd(sre);
    }

    public void sessionCreated(HttpSessionEvent event) {
        getSessionListener().sessionCreated(event);
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        getSessionListener().sessionDestroyed(event);
    }

    private HttpSessionListener getSessionListener() {
        if (sessionListener == null) {
            URI uri = URI.create(Names.RUNTIME_NAME + "/WebApplicationActivator");
            sessionListener = getComponent(HttpSessionListener.class, uri);
        }
        return sessionListener;
    }

}
