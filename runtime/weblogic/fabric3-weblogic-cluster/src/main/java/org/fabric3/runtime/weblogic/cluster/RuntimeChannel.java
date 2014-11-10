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
package org.fabric3.runtime.weblogic.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An RMI object that facilities communication between runtimes using the WebLogic clustered JNDI tree.
 * <p/>
 * This class and any referenced classes must be on the WebLogic server classpath as required by RMI dynamic stub generation.
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
