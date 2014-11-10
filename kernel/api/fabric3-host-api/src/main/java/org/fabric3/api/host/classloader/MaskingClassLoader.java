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
 */
package org.fabric3.api.host.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Prevents packages and resources from being loaded by child classloaders. Used to allow a Fabric3 runtime to load alternative versions or
 * implementations of classes visible on the system classpath, including JDK libraries such as JAXB.
 */
public class MaskingClassLoader extends ClassLoader {

    private static final Enumeration<URL> EMPTY = new Enumeration<URL>() {
        public boolean hasMoreElements() {
            return false;
        }

        public URL nextElement() {
            throw new NoSuchElementException();
        }
    };

    private String[] packageMasks;
    private boolean maskResources;

    /**
     * Constructor that masks one or more Java packages.
     *
     * @param parent       the parent classloader
     * @param packageMasks the packages to mask
     */
    public MaskingClassLoader(ClassLoader parent, String... packageMasks) {
       this(parent, packageMasks, false);
    }

    /**
     * Constructor that masks one or more Java packages and resources.
     *
     * @param parent        the parent classloader
     * @param packageMasks  the packages to mask
     * @param maskResources true if resources should be masked. For performance reasons (String compare is too slow for resources since they are not
     *                      cached like class bytecode), either all resources are masked or none are.
     */
    public MaskingClassLoader(ClassLoader parent, String[] packageMasks, boolean maskResources) {
        super(parent);
        this.packageMasks = packageMasks;
        this.maskResources = maskResources;
    }

    public URL getResource(String name) {
        if (maskResources) {
            return null;
        }
        return super.getResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        if (maskResources){
            return EMPTY;
        }
        return super.getResources(name);
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String mask : packageMasks) {
            if (name.startsWith(mask))
                throw new ClassNotFoundException(name);
        }
        return super.loadClass(name, resolve);
    }

    protected URL findResource(String name) {
        if (maskResources){
            return null;
        }
       return super.findResource(name);
    }

    protected Enumeration<URL> findResources(String name) throws IOException {
        if (maskResources) {
            return EMPTY;
        }
        return super.findResources(name);
    }

}
