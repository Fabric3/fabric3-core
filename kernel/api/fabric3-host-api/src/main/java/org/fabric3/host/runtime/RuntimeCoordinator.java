/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 * Manages the lifecycle of a Fabric3 runtime instance.
 */
public interface RuntimeCoordinator {

    /**
     * Returns the runtime state.
     *
     * @return the runtime state
     */
    RuntimeState getState();

    /**
     * Prepares the runtime, synchronizes it with the domain, and places it in a state to receive requests. Equivalent to calling {@link #prepare()}
     * and {@link #joinAndStart()}.
     *
     * @throws InitializationException if an error occurs starting the runtime
     */
    void start() throws InitializationException;

    /**
     * Prepares the runtime by performing bootstrap, extension initialization, and local recovery. Used in runtime hosts that require additional
     * initialization steps prior to placing Fabric3 in a state to receive requests.
     *
     * @throws InitializationException if an error occurs preparing the runtime
     */
    public void prepare() throws InitializationException;

    /**
     * Performs domain synchronization, domain recovery and places the runtime in a state to receive requests.
     */
    public void joinAndStart();

    /**
     * Shuts the runtime down, stopping it from receiving requests and detaching it from the domain. In-flight synchronous operations will be allowed
     * to proceed to completion.
     *
     * @throws ShutdownException if an error occurs shutting down the runtime
     */
    void shutdown() throws ShutdownException;
}


