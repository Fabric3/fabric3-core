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
package org.fabric3.api.node;

import java.net.URL;
import java.util.Map;

/**
 * Main API for interfacing with a service fabric.
 */
public interface Fabric {

    /**
     * Adds a profile to the fabric configuration. The profile name is either the full artifact id without version information or its shortened for, i.e.
     * without the 'profile-' prefix.
     *
     * This method must be called before {@link #start()}.
     *
     * @param name the profile name
     * @return the fabric
     */
    Fabric addProfile(String name);

    /**
     * Adds a profile to the fabric configuration.
     *
     * This method must be called before {@link #start()}.
     *
     * @param location the profile location
     * @return the fabric
     */
    Fabric addProfile(URL location);

    /**
     * Adds an extension to the fabric configuration. The profile name is the full artifact id without version information.
     *
     * This method must be called before {@link #start()}.
     *
     * @param name the extension name
     * @return the fabric
     */
    Fabric addExtension(String name);

    /**
     * Adds an extension to the fabric configuration.
     *
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
     * Creates and returns the dispatcher for a transport identified by the given interface.
     *
     * @param interfaze  the dispatcher interface
     * @param properties optional transport properties
     * @return the dispatcher or null if not found
     */
    <T> T createTransportDispatcher(Class<T> interfaze, Map<String, Object> properties);

    /**
     * Registers an instance as a system component. This method must be called before {@link #start()}}.
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
