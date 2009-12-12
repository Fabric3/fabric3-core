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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.namespace.QName;

import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_COMPOSITE_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_COMPOSITE_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPONENT_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_NAMESPACE_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_PARAM;
import static org.fabric3.runtime.webapp.Constants.DOMAIN_PARAM;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_ATTRIBUTE;

/**
 * Launches a Fabric3 runtime in a web application, loading information from servlet context parameters. This listener manages one runtime per servlet
 * context; the lifecycle of that runtime corresponds to the the lifecycle of the associated servlet context.
 * <p/>
 * The <code>web.xml</code> of a web application embedding Fabric3 must have entries for this listener and {@link Fabric3ContextListener}. The latter
 * notifies the runtime of session creation and expiration events through a "bridging" contract, {@link WebappRuntime}.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3ContextListener implements ServletContextListener {

    private RuntimeCoordinator coordinator;

    public void contextInitialized(ServletContextEvent event) {

        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        ServletContext servletContext = event.getServletContext();
        WebappUtil utils = getUtils(servletContext);
        WebappRuntime runtime;
        WebAppMonitor monitor = null;
        try {
            String defaultComposite = "WebappComposite";
            String compositeNamespace = utils.getInitParameter(COMPOSITE_NAMESPACE_PARAM, null);
            String compositeName = utils.getInitParameter(COMPOSITE_PARAM, defaultComposite);
            URI componentId = new URI(utils.getInitParameter(COMPONENT_PARAM, "webapp"));
            String scdlPath = utils.getInitParameter(APPLICATION_COMPOSITE_PATH_PARAM, APPLICATION_COMPOSITE_PATH_DEFAULT);
            URL scdl = servletContext.getResource(scdlPath);
            if (scdl == null) {
                throw new InitializationException("Web composite not found");
            }
            runtime = createRuntime(webappClassLoader, servletContext, utils);
            monitor = runtime.getMonitorFactory().getMonitor(WebAppMonitor.class);
            coordinator = utils.getCoordinator(webappClassLoader);
            BootConfiguration configuration = createBootConfiguration(runtime, webappClassLoader, utils);
            coordinator.setConfiguration(configuration);

            coordinator.start();
            servletContext.setAttribute(RUNTIME_ATTRIBUTE, runtime);
            monitor.started();
            // deploy the application composite
            QName qName = new QName(compositeNamespace, compositeName);
            runtime.deploy(qName, componentId);
            monitor.compositeDeployed(qName);
        } catch (ValidationException e) {
            // print out the validation errors
            monitor.contributionErrors(e.getMessage());
            throw new Fabric3InitException("Errors were detected in the web application contribution");
        } catch (AssemblyException e) {
            // print out the deployment errors
            monitor.deploymentErrors(e.getMessage());
            throw new Fabric3InitException("Deployment errors were detected");
        } catch (Fabric3RuntimeException e) {
            if (monitor != null) {
                monitor.runError(e);
            }
            throw e;
        } catch (Throwable e) {
            if (monitor != null) {
                monitor.runError(e);
            }
            throw new Fabric3InitException(e);
        }
    }

    private WebappRuntime createRuntime(ClassLoader webappClassLoader, ServletContext context, WebappUtil utils) {

        try {

            File baseDir = new File(URLDecoder.decode(context.getResource("/WEB-INF/lib/").getFile(), "UTF-8"));
            File tempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
            tempDir.mkdir();
            URI domain = new URI(utils.getInitParameter(DOMAIN_PARAM, "fabric3://domain"));
            WebappHostInfo info = new WebappHostInfoImpl(context, domain, baseDir, tempDir);

            WebappRuntime runtime = utils.createRuntime(webappClassLoader);
            MonitorFactory factory = utils.createMonitorFactory(webappClassLoader);
            MBeanServer mBeanServer = utils.createMBeanServer();


            RuntimeConfiguration<WebappHostInfo> configuration =
                    new RuntimeConfiguration<WebappHostInfo>(webappClassLoader, info, factory, mBeanServer);

            runtime.setConfiguration(configuration);

            return runtime;

        } catch (URISyntaxException e) {
            throw new Fabric3InitException(e);
        } catch (UnsupportedEncodingException e) {
            throw new Fabric3InitException(e);
        } catch (MalformedURLException e) {
            throw new Fabric3InitException(e);
        }

    }

    /*
     * Creates the boot configuration.
     */
    private BootConfiguration createBootConfiguration(WebappRuntime runtime,
                                                      ClassLoader webappClassLoader,
                                                      WebappUtil utils) throws InitializationException {

        BootConfiguration configuration = new BootConfiguration();
        configuration.setBootClassLoader(webappClassLoader);

        // create the runtime bootrapper
        URL systemScdl = utils.getSystemScdl(webappClassLoader);
        ScdlBootstrapper bootstrapper = utils.getBootstrapper(webappClassLoader);
        bootstrapper.setScdlLocation(systemScdl);
        configuration.setBootstrapper(bootstrapper);

        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.runtime.webapp", Names.VERSION);
        exportedPackages.put("org.fabric3.container.web.spi", Names.VERSION);
        configuration.setExportedPackages(exportedPackages);

        // process extensions
        ServletContext context = runtime.getHostInfo().getServletContext();
        List<ContributionSource> extensions = getExtensionContributions("/WEB-INF/lib/f3Extensions.properties", context);
        configuration.setExtensionContributions(extensions);

        configuration.setRuntime(runtime);
        return configuration;

    }

    /*
     * Gets the extension contributions.
     */
    private List<ContributionSource> getExtensionContributions(String extensionDefinitions, ServletContext context) throws InitializationException {

        InputStream stream = context.getResourceAsStream(extensionDefinitions);
        if (stream == null) {
            return Collections.emptyList();
        }

        Properties props = new Properties();
        try {
            props.load(stream);
        } catch (IOException e) {
            throw new InitializationException(e);
        }

        List<URL> files = new ArrayList<URL>();
        for (Object key : props.keySet()) {
            try {
                URL url = context.getResource("/WEB-INF/lib/" + key).toURI().toURL();
                files.add(url);
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            } catch (URISyntaxException e) {
                throw new AssertionError(e);
            }
        }

        if (!files.isEmpty()) {
            // contribute and activate extensions if they exist in the runtime domain
            List<ContributionSource> sources = new ArrayList<ContributionSource>();
            for (URL location : files) {
                URI uri = URI.create(location.getPath());
                ContributionSource source = new FileContributionSource(uri, location, -1, new byte[0]);
                sources.add(source);

            }
            return sources;
        }

        return Collections.emptyList();

    }

    /**
     * Can be overridden for tighter host integration.
     *
     * @param servletContext Servlet context for the runtime.
     * @return Webapp util to be used.
     */
    protected WebappUtil getUtils(ServletContext servletContext) {
        return new WebappUtilImpl(servletContext);
    }

    /**
     * Invoked when the servlet context is destroyed. This is used to shutdown the runtime.
     */
    public void contextDestroyed(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();
        WebappRuntime runtime = (WebappRuntime) servletContext.getAttribute(RUNTIME_ATTRIBUTE);

        if (runtime != null) {
            servletContext.removeAttribute(RUNTIME_ATTRIBUTE);
            runtime.getMonitorFactory().getMonitor(WebAppMonitor.class).stopped();
        }

        try {
            if (coordinator == null) {
                return;
            }
            coordinator.shutdown();
        } catch (ShutdownException e) {
            servletContext.log("Error shutting runtume down", e);
        }

    }


}
