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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.builder.classloader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.container.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.host.Names.HOST_CONTRIBUTION;

/**
 * Default implementation of ClassLoaderBuilder.
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
        initializeSysPaths(monitor);

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
            List<URL> classpath = new ArrayList<>();
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
     * @param monitor the monitor
     */
    private void initializeSysPaths(ClassLoaderBuilderMonitor monitor) {
        try {
            sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // log message since sys_paths may not be supported on all JVMs, e.g. J9
            monitor.nativeLibrariesNotSupported();
        }
    }

}
