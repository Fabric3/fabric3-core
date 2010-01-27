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
package org.fabric3.fabric.builder.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import static org.fabric3.host.Names.HOST_CONTRIBUTION;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 * Default implementation of ClassLoaderBuilder.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClassLoaderBuilderImpl implements ClassLoaderBuilder {

    private ClassLoaderWireBuilder wireBuilder;
    private ClassLoaderRegistry classLoaderRegistry;
    private ClasspathProcessorRegistry classpathProcessorRegistry;
    private ComponentManager componentManager;
    private ContributionResolver resolver;
    private boolean classLoaderIsolation;

    public ClassLoaderBuilderImpl(@Reference ClassLoaderWireBuilder wireBuilder,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference ComponentManager componentManager,
                                  @Reference ContributionResolver resolver,
                                  @Reference HostInfo info) {
        this.wireBuilder = wireBuilder;
        this.classLoaderRegistry = classLoaderRegistry;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.componentManager = componentManager;
        this.resolver = resolver;
        classLoaderIsolation = info.supportsClassLoaderIsolation();
    }

    public void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI uri = definition.getUri();
        if (classLoaderRegistry.getClassLoader(uri) != null) {
            /*
             The classloader was already loaded. The classloader will already be created if: it is the boot classloader; the environment is
             single-VM as classloaders are shared between the contribution and runtime infrastructure; two composites are deployed individually
             from the same contribution.
             */
            return;
        }
        if (classLoaderIsolation) {
            buildIsolatedClassLoaderEnvironment(definition);
        } else {
            buildCommonClassLoaderEnvironment(definition);
        }
    }

    private void buildCommonClassLoaderEnvironment(PhysicalClassLoaderDefinition definition) {
        URI uri = definition.getUri();
        // Create an alias to the host classloader which contains all contribution artifacts in a non-isolated environment.
        // This simulates multiple classloaders
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CONTRIBUTION);
        classLoaderRegistry.register(uri, hostClassLoader);
    }

    private void buildIsolatedClassLoaderEnvironment(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI uri = definition.getUri();
        URL[] classpath = resolveClasspath(definition.getContributionUri());

        // build the classloader using the locally cached resources
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CONTRIBUTION);
        MultiParentClassLoader loader = new MultiParentClassLoader(uri, classpath, hostClassLoader);
        for (PhysicalClassLoaderWireDefinition wireDefinition : definition.getWireDefinitions()) {
            wireBuilder.build(loader, wireDefinition);
        }
        classLoaderRegistry.register(uri, loader);
    }

    public void destroy(URI uri) throws ClassLoaderBuilderException {
        ClassLoader loader = classLoaderRegistry.getClassLoader(uri);
        assert loader != null;
        List<Component> components = componentManager.getComponents();
        // remove the classloader if there are no components that reference it
        for (Component component : components) {
            if (uri.equals(component.getClassLoaderId())) {
                return;
            }
        }
        try {
            // release the previously resolved contribution
            resolver.release(uri);
        } catch (ResolutionException e) {
            throw new ClassLoaderBuilderException("Error releasing artifact: " + uri.toString(), e);
        }
        classLoaderRegistry.unregister(uri);
    }

    /**
     * Resolves classpath urls.
     *
     * @param uri uri to resolve
     * @return the resolved classpath urls
     * @throws ClassLoaderBuilderException if an error occurs resolving a url
     */
    private URL[] resolveClasspath(URI uri) throws ClassLoaderBuilderException {

        List<URL> classpath = new ArrayList<URL>();

        try {
            // resolve the remote contributions and cache them locally
            URL resolvedUrl = resolver.resolve(uri);
            // introspect and expand if necessary
            classpath.addAll(classpathProcessorRegistry.process(resolvedUrl));
        } catch (ResolutionException e) {
            throw new ClassLoaderBuilderException("Error resolving artifact: " + uri.toString(), e);
        } catch (IOException e) {
            throw new ClassLoaderBuilderException("Error processing: " + uri.toString(), e);
        }
        return classpath.toArray(new URL[classpath.size()]);

    }

}
