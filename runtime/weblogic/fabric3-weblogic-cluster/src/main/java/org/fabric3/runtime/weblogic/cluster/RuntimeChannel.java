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
*/
package org.fabric3.runtime.weblogic.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An RMI object that facilities communication between runtimes using the WebLogic clustered JNDI tree.
 * <p/>
 * This class and any referenced classes must be on the WebLogic server classpath as required by RMI dynamic stub generation.
 *
 * @version $Rev$ $Date$
 */
public interface RuntimeChannel extends Remote {

    /**
     * Returns true if the runtime referred to by this channel is active.
     *
     * @return true if the runtime referred to by this channel is active
     */
    boolean isActive();

    /**
     * Returns the runtime name. This corresponds to the WebLogic server name.
     *
     * @return the runtime name
     * @throws RemoteException if a remote communication error occurs
     */
    String getRuntimeName() throws RemoteException;

    /**
     * Sends a request-response command to the runtime.
     *
     * @param payload the serialized command
     * @return the serialized response
     * @throws RemoteException  if a remote communication error occurs
     * @throws ChannelException if an error processing the message occurs
     */
    byte[] sendSynchronous(byte[] payload) throws RemoteException, ChannelException;

    /**
     * Sends one-way command to the runtime.
     *
     * @param payload the serialized command
     * @throws RemoteException  if a remote communication error occurs
     * @throws ChannelException if an error processing the message occurs
     */
    void send(byte[] payload) throws RemoteException, ChannelException;

    /**
     * Publishes a message to the runtime. Used for dynamic channels.
     *
     * @param payload the serialized message
     * @throws RemoteException  if a remote communication error occurs
     * @throws ChannelException if an error processing the message occurs
     */
    void publish(byte[] payload) throws RemoteException, ChannelException;
}
