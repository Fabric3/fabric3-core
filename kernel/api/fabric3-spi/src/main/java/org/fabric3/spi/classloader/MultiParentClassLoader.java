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
package org.fabric3.spi.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import sun.security.util.SecurityConstants;

/**
 * A classloader implementation that supports a multi-parent hierarchy and extension resolution mechanism. Class resolution is performed in the
 * following order:
 * <pre>
 * <ul>
 *   <li>Parents are searched. Parents will delegate to their classloader hierarchy.
 *   <li>If a resource is not found, the current classloader is searched.
 *   <li>If a resource is not found, extension classloaders are searched. Extension classloaders will not delegate to their classloader hierarchy.
 * </ul>
 * </pre>
 * The extension mechanism allows classes to be dyamically loaded via Class.forName() and ClassLoader.loadClass(). This is used to accomodate
 * contributions and libraries that rely on Java reflection to add additional capabilities provided by another contribution. Since reslution is
 * performed dynamically, cycles between classloaders are supported where one classloader is a parent of the other and the former is an extension of
 * the latter.
 * <p/>
 * Each classloader has a name that can be used to reference it in the runtime.
 *
 * @version $Rev$ $Date$
 */
public class MultiParentClassLoader extends URLClassLoader {
    private static final URL[] NOURLS = {};

    private final URI name;
    private final List<ClassLoader> parents = new CopyOnWriteArrayList<ClassLoader>();
    private final List<MultiParentClassLoader> extensions = new CopyOnWriteArrayList<MultiParentClassLoader>();

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
        List<ClassLoader> list = new ArrayList<ClassLoader>();
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
     * Resolves a resource only in this classloader. Note this method does not delegate to parent classloaders.
     *
     * @param name the resource name
     * @return the resource URL or null if not found
     */
    public URL findExtensionResource(String name) {
        // look in our classpath
        return super.findResource(name);
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
                                clazz = extension.findClass(name);
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
            } catch (NoClassDefFoundError e) {
                throw e;
            } catch (ClassNotFoundException e) {
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
        Set<URL> resources = new LinkedHashSet<URL>();
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
        return Collections.enumeration(resources);
    }


    public String toString() {
        return name.toString();
    }
}
