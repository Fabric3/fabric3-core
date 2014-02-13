/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
 */
package org.fabric3.api.host.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;

import org.fabric3.api.host.util.CompositeEnumeration;

/**
 * A classloader that implements a delegation strategy for resolving resources in a classloader hierarchy. This implementation is used to guarantee
 * consistent resource resolution across VMs. Specifically, the IBM J9 VM does not use delegation in its implementation of ClassLoader.getResource()
 * and ClassLoader.getResources(). Instead, the J9 ClassLoader walks the parent hierarchy and calls ClassLoader.findResource() and
 * ClassLoader.findResources() respectively. After this is done, the J9 ClassLoader attempts to resolve resources against the bootstrap classpath.
 * Doing the latter circumvents masking resources present in parent classloaders and on the boot classpath. To avoid this, the classloader overrides
 * resource resolution using a parent delegation strategy.
 * <p/>
 * Hierarchical classloaders that are not masking classloaders instantiated by Fabric3 should inherit from this class to ensure proper resource
 * resolution.
 * <p/>
 * Note that this classloader must have a parent as it does not resolve resources against the boot classpath. Doing so requires accessing
 * vendor-specific APIs and this implementation is intended to work across VMs.
 */
public class DelegatingResourceClassLoader extends URLClassLoader {

    public DelegatingResourceClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        checkParent(parent);
    }

    public DelegatingResourceClassLoader(URL[] urls) {
        this(urls, null);
    }

    public DelegatingResourceClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        checkParent(parent);
    }

    public URL getResource(String name) {
        URL url = getParent().getResource(name);
        return url == null ? findResource(name) : url;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration[] resources = new Enumeration[2];
        resources[0] = getParent().getResources(name);
        resources[1] = findResources(name);
        return new CompositeEnumeration<>(resources);
    }

    private void checkParent(ClassLoader parent) {
        if (parent == null) {
            throw new UnsupportedOperationException("This classloader cannot be used as a top-level classloader");
        }
    }

}
