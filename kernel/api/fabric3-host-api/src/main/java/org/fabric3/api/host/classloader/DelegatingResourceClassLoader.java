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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
 *
 * Hierarchical classloaders that are not masking classloaders instantiated by Fabric3 should inherit from this class to ensure proper resource
 * resolution.
 *
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
