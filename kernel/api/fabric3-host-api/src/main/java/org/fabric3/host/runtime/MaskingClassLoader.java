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
*/
package org.fabric3.host.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Prevents packages and resources from being loaded by child classloaders. Used to allow a Fabric3 runtime to load alternative versions or
 * implementations of classes visible on the system classpath, including JDK libraries such as JAXB.
 *
 * @version $Rev$ $Date$
 */
public class MaskingClassLoader extends ClassLoader {
    private static final ResourceFilter EMPTY_FILTER = new ResourceFilter(new String[0]);

    private String[] packageMasks;
    private ResourceFilter filter = EMPTY_FILTER;

    /**
     * Constructor that masks one or more Java packages.
     *
     * @param parent       the parent classloader
     * @param packageMasks the packages to mask
     */
    public MaskingClassLoader(ClassLoader parent, String... packageMasks) {
        super(parent);
        this.packageMasks = packageMasks;
    }

    /**
     * Constructor that masks one or more Java packages and resources.
     *
     * @param parent        the parent classloader
     * @param packageMasks  the packages to mask
     * @param resourceMasks the resource pattern to mask. For performance purposes, only a String.contains() will be performed for each mask value.
     */
    public MaskingClassLoader(ClassLoader parent, String[] packageMasks, String[] resourceMasks) {
        this(parent, packageMasks);
        filter = new ResourceFilter(resourceMasks);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String mask : packageMasks) {
            if (name.startsWith(mask))
                throw new ClassNotFoundException(name);
        }
        return super.loadClass(name, resolve);
    }

    public URL getResource(String name) {
        URL url = super.getResource(name);
        return filter.filterResource(url);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> enumeration = super.getResources(name);
        return filter.filterResources(enumeration);
    }

    protected URL findResource(String name) {
        URL url = super.findResource(name);
        return filter.filterResource(url);
    }

    protected Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> enumeration = super.findResources(name);
        return filter.filterResources(enumeration);
    }

}
