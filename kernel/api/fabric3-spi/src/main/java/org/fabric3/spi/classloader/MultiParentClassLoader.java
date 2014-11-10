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
package org.fabric3.spi.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import sun.security.util.SecurityConstants;

import org.fabric3.api.host.classloader.DelegatingResourceClassLoader;

/**
 * A classloader implementation that supports a multi-parent hierarchy and extension resolution mechanism. Class resolution is performed in the following
 * order:
 * <pre>
 * <ul>
 *   <li>Parents are searched. Parents will delegate to their classloader hierarchy.
 *   <li>If a resource is not found, the current classloader is searched.
 *   <li>If a resource is not found, extension classloaders are searched. Extension classloaders will not delegate to their classloader hierarchy.
 * </ul>
 * </pre>
 * The extension mechanism allows classes to be dynamically loaded via Class.forName() and ClassLoader.loadClass(). This is used to accommodate contributions
 * and libraries that rely on Java reflection to add additional capabilities provided by another contribution. Since resolution is performed dynamically, cycles
 * between classloaders are supported where one classloader is a parent of the other and the former is an extension of the latter.
 * <p/>
 * Each classloader has a name that can be used to reference it in the runtime.
 */
public class MultiParentClassLoader extends DelegatingResourceClassLoader {
    private static final URL[] NOURLS = {};

    private final URI name;

    private final List<ClassLoader> parents = new CopyOnWriteArrayList<>();
    private final List<MultiParentClassLoader> extensions = new CopyOnWriteArrayList<>();

    /**
     * Constructs a classloader with a name and a single parent.
     *
     * @param name   a name used to identify this classloader
     * @param parent the initial parent
     */
    public MultiParentClassLoader(URI name, ClassLoader parent) {
        this(name, NOURLS, parent);
    }

    /**
     * Constructs a classloader with a name, a set of resources and a single parent.
     *
     * @param name   a name used to identify this classloader
     * @param urls   the URLs from which to load classes and resources
     * @param parent the initial parent
     */
    public MultiParentClassLoader(URI name, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        if (parent == null) {
            throw new IllegalArgumentException("Parent classloader cannot be null");
        }
        this.name = name;
    }

    /**
     * Add a resource URL to this classloader's classpath. The "createClassLoader" RuntimePermission is required.
     *
     * @param url an additional URL from which to load classes and resources
     */
    public void addURL(URL url) {
        // Require RuntimePermission("createClassLoader")
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
        }
        super.addURL(url);
    }

    /**
     * Add a parent to this classloader. The "createClassLoader" RuntimePermission is required.
     *
     * @param parent an additional parent classloader
     */
    public void addParent(ClassLoader parent) {
        // Require RuntimePermission("createClassLoader")
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
        }
        if (parent != null) {
            parents.add(parent);
        }
    }

    /**
     * Returns the name of this classloader.
     *
     * @return the name of this classloader
     */
    public URI getName() {
        return name;
    }

    /**
     * Returns the parent classLoaders. The "getClassLoader" RuntimePermission is required.
     *
     * @return the parent classLoaders
     */
    public List<ClassLoader> getParents() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
        }
        List<ClassLoader> list = new ArrayList<>();
        if (getParent() != null) {
            list.add(getParent());
        }
        list.addAll(parents);
        return list;
    }

    /**
     * Adds a classloader as an extension of this classloader.
     *
     * @param classloader the extension classloader.
     */
    public void addExtensionClassLoader(MultiParentClassLoader classloader) {
        extensions.add(classloader);
    }

    /**
     * Removes a classloader as an extension of this classloader.
     *
     * @param classloader the extension classloader.
     */
    public void removeExtensionClassLoader(MultiParentClassLoader classloader) {
        extensions.remove(classloader);
    }

    /**
     * Resolves a resource only in this classloader. Note this method does not delegate to parent classloaders.
     *
     * @param name the resource name
     * @return the resource URL or null if not found
     */
    public URL findExtensionResource(String name) {
        // look in our classpath
        return super.findResource(name);
    }

    public Enumeration<URL> findExtensionResources(String name) throws IOException {
        // look in our classpath
        return super.findResources(name);
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // look for already loaded classes
        Class<?> clazz = findLoadedClass(name);
        if (clazz == null) {
            try {
                // look in the primary parent
                try {
                    clazz = Class.forName(name, resolve, getParent());
                } catch (ClassNotFoundException e) {
                    // continue
                }
                if (clazz == null) {
                    // look in our parents
                    for (ClassLoader parent : parents) {
                        try {
                            clazz = parent.loadClass(name);
                            break;
                        } catch (ClassNotFoundException e) {
                            continue;
                        }
                    }
                }
                // look in our classpath
                if (clazz == null) {
                    try {
                        clazz = findClass(name);
                    } catch (ClassNotFoundException e) {
                        // look in extensions
                        for (MultiParentClassLoader extension : extensions) {
                            // check first to see if class is already loaded
                            clazz = extension.findLoadedClass(name);
                            if (clazz == null) {
                                try {
                                    clazz = extension.findClass(name);
                                } catch (ClassNotFoundException ex) {
                                    // ignore
                                }
                            }
                            if (clazz != null) {
                                break;
                            }
                        }
                        if (clazz == null) {
                            throw e;
                        }
                    }
                }
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                throw e;
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    protected Class<?> findClass(String string) throws ClassNotFoundException {
        return super.findClass(string);
    }

    public URL findResource(String name) {
        // look in our parents
        for (ClassLoader parent : parents) {
            URL resource = parent.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        // look in our classpath
        URL resource = super.findResource(name);
        if (resource == null) {
            // look in extensions
            for (MultiParentClassLoader extension : extensions) {
                resource = extension.findExtensionResource(name);
                if (resource != null) {
                    return resource;
                }
            }
        }
        return resource;
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        // LinkedHashSet because we want all resources in the order found but no duplicates
        Set<URL> resources = new LinkedHashSet<>();
        for (ClassLoader parent : parents) {
            Enumeration<URL> parentResources = parent.getResources(name);
            while (parentResources.hasMoreElements()) {
                resources.add(parentResources.nextElement());
            }
        }
        Enumeration<URL> myResources = super.findResources(name);
        while (myResources.hasMoreElements()) {
            resources.add(myResources.nextElement());
        }
        for (MultiParentClassLoader extension : extensions) {
            Enumeration<URL> extensionResources = extension.findExtensionResources(name);
            while (extensionResources.hasMoreElements()) {
                resources.add(extensionResources.nextElement());
            }
        }
        return Collections.enumeration(resources);
    }

    public String toString() {
        return name.toString();
    }
}
