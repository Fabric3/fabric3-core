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
package org.fabric3.api.node;

import java.net.URL;

/**
 * Main API for interfacing with a service fabric.
 */
public interface Fabric {

    /**
     * Adds a profile to the fabric configuration. The profile name is either the full artifact id without version information or its shortened for, i.e.
     * without the 'profile-' prefix.
     * <p/>
     * This method must be called before {@link #start()}.
     *
     * @param name the profile name
     * @return the fabric
     */
    Fabric addProfile(String name);

    /**
     * Adds a profile to the fabric configuration.
     * <p/>
     * This method must be called before {@link #start()}.
     *
     * @param location the profile location
     * @return the fabric
     */
    Fabric addProfile(URL location);

    /**
     * Adds an extension to the fabric configuration. The profile name is the full artifact id without version information.
     * <p/>
     * This method must be called before {@link #start()}.
     *
     * @param name the extension name
     * @return the fabric
     */
    Fabric addExtension(String name);

    /**
     * Adds an extension to the fabric configuration.
     * <p/>
     * This method must be called before {@link #start()}.
     *
     * @param location the extension location
     * @return the fabric
     */
    Fabric addExtension(URL location);

    /**
     * Starts the connection to the fabric.
     *
     * @return the fabric
     * @throws FabricException if there is an error connecting.
     */
    Fabric start() throws FabricException;

    /**
     * Stops the connection to the fabric.
     *
     * @return the fabric
     * @throws FabricException if there an error stopping the connection
     */
    Fabric stop() throws FabricException;

    /**
     * Registers an instance as a system component. This method is typically called before {@link #start()}}.
     *
     * @param interfaze the service interface of the instance
     * @param instance  the instance
     * @return the fabric
     * @throws FabricException if there is a registration error
     */
    <T> Fabric registerSystemService(Class<T> interfaze, T instance) throws FabricException;

    /**
     * Returns the fabric domain
     *
     * @return the domain
     */
    Domain getDomain();

}
