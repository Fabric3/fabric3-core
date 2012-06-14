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
package org.fabric3.fabric.builder.classloader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.Library;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

import static org.fabric3.host.Names.HOST_CONTRIBUTION;

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
    private ContributionResolver resolver;
    private ClassLoaderTracker tracker;
    private List<ClassLoaderListener> listeners;
    private HostInfo info;
    private MetaDataStore metaDataStore;
    private Field sysPathsField;

    public ClassLoaderBuilderImpl(@Reference ClassLoaderWireBuilder wireBuilder,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference ClasspathProcessorRegistry classpathProcessorRegistry,
                                  @Reference ContributionResolver resolver,
                                  @Reference ClassLoaderTracker tracker,
                                  @Reference MetaDataStore metaDataStore,
                                  @Reference HostInfo info,
                                  @Monitor ClassLoaderBuilderMonitor monitor) {
        this.wireBuilder = wireBuilder;
        this.classLoaderRegistry = classLoaderRegistry;
        this.classpathProcessorRegistry = classpathProcessorRegistry;
        this.resolver = resolver;
        this.tracker = tracker;
        this.metaDataStore = metaDataStore;
        this.info = info;
        this.listeners = Collections.emptyList();
        initializeSysPaths(info, monitor);

    }

    @Reference(required = false)
    public void setListeners(List<ClassLoaderListener> listeners) {
        this.listeners = listeners;
    }

    public void build(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI uri = definition.getUri();
        int count = tracker.increment(uri);
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(uri);
        if (classLoader != null) {
            // The classloader was already loaded. The classloader will already be created if: it is the boot classloader; the environment is
            // single-VM as classloaders are shared between the contribution and runtime infrastructure; two composites are deployed individually
            // from the same contribution.
            for (PhysicalClassLoaderWireDefinition wireDefinition : definition.getWireDefinitions()) {
                URI target = wireDefinition.getTargetClassLoader();
                classLoader = classLoaderRegistry.getClassLoader(target);
                tracker.incrementImported(classLoader);
            }
            notifyListenersBuild(count, classLoader);
            return;
        }
        if (info.supportsClassLoaderIsolation()) {
            buildIsolatedClassLoaderEnvironment(definition);
        } else {
            buildCommonClassLoaderEnvironment(definition);
        }
        notifyListenersBuild(count, classLoader);
    }

    public void destroy(URI uri) throws ClassLoaderBuilderException {
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(uri);
        int val = tracker.decrement(classLoader);
        if (val == 0 && metaDataStore.find(uri) == null) {
            // Note the MetaDataStore is used to determine if a contribution classloader must be tracked. If a contribution is registered in the
            // store, it is installed as an extension of the base runtime distribution and should only be uninstalled explicitly.
            try {
                classLoaderRegistry.unregister(uri);
                // release the previously resolved contribution
                resolver.release(uri);
            } catch (ResolutionException e) {
                throw new ClassLoaderBuilderException("Error releasing artifact: " + uri.toString(), e);
            }
            for (ClassLoaderListener listener : listeners) {
                listener.onUndeploy(classLoader);
            }
        } else if (val == 0) {
            // single VM, do not remove the classloader since it is used by the installed contribution. Just notify listeners
            for (ClassLoaderListener listener : listeners) {
                listener.onUndeploy(classLoader);
            }
        }
    }

    private void notifyListenersBuild(int count, ClassLoader classLoader) {
        if (count == 1) {
            for (ClassLoaderListener listener : listeners) {
                listener.onDeploy(classLoader);
            }
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
        // build the classloader using the locally cached resources
        ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(HOST_CONTRIBUTION);
        MultiParentClassLoader loader;
        if (definition.isProvisionArtifact()) {
            URL[] classpath = resolveClasspath(definition);
            loader = new MultiParentClassLoader(uri, classpath, hostClassLoader);
            setSysPathsField(loader);
        } else {
            loader = new MultiParentClassLoader(uri, hostClassLoader);
        }
        for (PhysicalClassLoaderWireDefinition wireDefinition : definition.getWireDefinitions()) {
            wireBuilder.build(loader, wireDefinition);
            URI target = wireDefinition.getTargetClassLoader();
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(target);
            tracker.incrementImported(classLoader);
        }
        classLoaderRegistry.register(uri, loader);
    }

    /**
     * Resolves classpath urls.
     *
     * @param definition the physical classpath definition
     * @return the resolved classpath urls
     * @throws ClassLoaderBuilderException if an error occurs resolving a url
     */
    private URL[] resolveClasspath(PhysicalClassLoaderDefinition definition) throws ClassLoaderBuilderException {
        URI uri = definition.getUri();
        try {
            // resolve the remote contributions and cache them locally
            URL resolvedUrl = resolver.resolve(uri);
            // introspect and expand if necessary
            List<URL> classpath = new ArrayList<URL>();
            List<Library> libraries = definition.getLibraries();
            List<URL> archiveClasspath = classpathProcessorRegistry.process(resolvedUrl, libraries);
            classpath.addAll(archiveClasspath);
            return classpath.toArray(new URL[classpath.size()]);
        } catch (ResolutionException e) {
            throw new ClassLoaderBuilderException("Error resolving artifact: " + uri.toString(), e);
        } catch (IOException e) {
            throw new ClassLoaderBuilderException("Error processing: " + uri.toString(), e);
        }
    }

    /**
     * Sets the native libraries path by setting the classloader sysPathsField to null. This will force the classloader to reinitialize the field to
     * JAVA_LIBRARY_PATH.
     *
     * @param loader the classloader
     */
    private void setSysPathsField(MultiParentClassLoader loader) {
        if (sysPathsField == null) {
            // not supported on this JVM
            return;
        }
        try {
            sysPathsField.set(loader, null);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Performs a workaround to enable specifying the path for native libraries on a per-extension classloader basis.
     *
     * @param info    the runtime info
     * @param monitor the monitor
     */
    private void initializeSysPaths(HostInfo info, ClassLoaderBuilderMonitor monitor) {
        try {
            sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // fail silently since sys_paths may not be supported on all JVMs, e.g. J9
            if (RuntimeMode.PARTICIPANT == info.getRuntimeMode()) {
                // only issue info if on participant since contribution loading will issue a warning as well. In VM mode where the two subsystems
                // are collocated, this would result in duplicate messages
                monitor.nativeLibrariesNotSupported();
            }
        }
    }


}
