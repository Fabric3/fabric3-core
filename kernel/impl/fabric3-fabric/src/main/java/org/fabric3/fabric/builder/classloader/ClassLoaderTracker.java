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

import java.net.URI;

/**
 * Tracks classloader usage across deployments on a participant runtime.
 * <p/>
 * SCA's import/export mechanism allows contribution classloaders to be referenced by multiple deployments (e.g. a contribution with deployed
 * components may import another contribution that exports an API package). This service tracks usage, signaling when a contribution classloader is no
 * longer referenced and may be removed.
 *
 * @version $Rev: 8734 $ $Date: 2010-03-22 15:04:53 +0100 (Mon, 22 Mar 2010) $
 */
public interface ClassLoaderTracker {

    /**
     * Returns true if the contribution classloader is in use.
     *
     * @param uri the contribution classloader id
     * @return true if the contribution classloader is in use; otherwise false
     */
    boolean isReferenced(URI uri);

    /**
     * Increments the use count for a contribution classloader.
     *
     * @param uri the contribution classloader id
     * @return the count after it is incremented
     */
    int increment(URI uri);

    /**
     * Increments the use count of all transitively imported classloaders starting with the given (parent) classloader.
     *
     * @param classLoader the imported classloader, which is generally a parent of the contribution classloader
     */
    void incrementImported(ClassLoader classLoader);

    /**
     * Decrements the in use count for a contribution classloader and all transitively imported contribution classloaders.
     *
     * @param classLoader the contribution classloader
     * @return the count after it is decremented
     */
    int decrement(ClassLoader classLoader);

}