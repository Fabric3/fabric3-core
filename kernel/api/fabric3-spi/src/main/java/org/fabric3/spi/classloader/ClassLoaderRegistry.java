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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.classloader;

import java.net.URI;
import java.util.Map;

/**
 * Registry for classloaders available to the local runtime.
 */
public interface ClassLoaderRegistry {
    /**
     * Register a ClassLoader with the runtime.
     *
     * @param id          a unique id for the classloader
     * @param classLoader the classloader to register
     */
    void register(URI id, ClassLoader classLoader);

    /**
     * Unregister the specified classloader from the system.
     *
     * @param id the id for the classloader
     * @return the classloader that was registered with the id, or null if none
     */
    ClassLoader unregister(URI id);

    /**
     * Returns the classloader registered with the supplied id, or null if none is registered.
     *
     * @param id the id for the classloader
     * @return the ClassLoader registered with that id, or null
     */
    ClassLoader getClassLoader(URI id);

    /**
     * Returns all registered classloaders.
     *
     * @return all registered classloaders
     */
    Map<URI, ClassLoader> getClassLoaders();

    /**
     * Load and define a class from a specific classloader.
     *
     * @param classLoaderId the id of the ClassLoader to use
     * @param className     the name of the class
     * @return the class
     * @throws ClassNotFoundException if the class could not be found by that classloader
     */
    Class<?> loadClass(URI classLoaderId, String className) throws ClassNotFoundException;

    /**
     * Load and define a class from a specific classloader.
     *
     * @param cl        the ClassLoader to use
     * @param className the name of the class
     * @return the class
     * @throws ClassNotFoundException if the class could not be found by that classloader
     */
    Class<?> loadClass(ClassLoader cl, String className) throws ClassNotFoundException;

}
