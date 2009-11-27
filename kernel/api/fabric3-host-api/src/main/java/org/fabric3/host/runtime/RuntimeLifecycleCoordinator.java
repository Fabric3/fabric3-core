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
package org.fabric3.host.runtime;

/**
 * Implementations manage the Fabric3 runtime lifecycle. This involves transitioning through a series of states:
 * <pre>
 * <ul>
 *      <li>BOOT PRIMORDIAL - the runtime is booted with and its domain containing system components is initialized.
 *      <li>INITIALIZE - extensions are registered and activated in the local runtime domain.
 *      <li>RECOVER - the runtime recovers and synchronizes its state with the application domain.
 *      <li>JOIN DOMIAN - the runtime instance discoveres and joins an application domain.
 *      <li>START - the runtime is prepared to receive and process requests
 *      <li>SHUTDOWN - the runtime has stopped prcessing synnchronous requests and detached from the domain.
 * </ul>
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public interface RuntimeLifecycleCoordinator {

    /**
     * Sets the bootstrap configuration. Thismust be done prior to booting the runtime.
     *
     * @param configuration the bootstrap configuration
     */
    void setConfiguration(BootConfiguration configuration);

    /**
     * Boots the runtime with its primordial components.
     *
     * @throws InitializationException if an error occurs booting the runtime
     */
    void bootPrimordial() throws InitializationException;

    /**
     * Initializes the runtime, including all system components
     *
     * @throws InitializationException if an error occurs initializing the runtime
     */
    void initialize() throws InitializationException;

    /**
     * Performs local recovery operations.
     *
     * @throws InitializationException if an error occurs performing recovery
     */
    void recover() throws InitializationException;

    /**
     * Joins the domain in a non-blocking fashion.
     *
     * @param timeout the timeout in milliseconds or -1 if the operation should wait indefinitely
     * @throws InitializationException if an error occurs joining the domain
     */
    void joinDomain(long timeout) throws InitializationException;

    /**
     * Start the runtime receiving requests.
     *
     * @throws InitializationException if an error occurs starting the runtime
     */
    void start() throws InitializationException;

    /**
     * Shuts the runtime down, stopping it from receiving requests and detaching it from the domain. In-flight synchronous operations will be allowed
     * to proceed to completion.
     *
     * @throws ShutdownException if an error ocurrs shutting down the runtime
     */
    void shutdown() throws ShutdownException;
}


