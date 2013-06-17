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
*/
package org.fabric3.host.classloader;

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
