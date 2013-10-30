/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime;

import java.net.URI;
import java.util.List;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Responsible for managing local senders and receivers. Unlike brokers in traditional hub-and-spoke messaging architectures, implementations do not
 * receive or forward messages; rather, senders connect directly to receivers.
 */
public interface ZeroMQWireBroker {

    /**
     * Connects a set of ordered invocation chains to a ZeroMQ XREQ sender. The Invocation chain order is used to match an invocation chain on the
     * receiving end to dispatch the invocation to.
     *
     * @param id         the connection id
     * @param uri        the target service URI
     * @param chains     the invocation chains
     * @param metadata the ZeroMQ metadata to configure the underlying socket
     * @param loader     the classloader to load invocation parameters with
     * @throws BrokerException if a connection error occurs
     */
    public void connectToSender(String id, URI uri, List<InvocationChain> chains, ZeroMQMetadata metadata, ClassLoader loader) throws BrokerException;

    /**
     * Releases a previous connection to a sender.
     *
     * @param id  the connection id
     * @param uri the target service URI
     * @throws BrokerException if a connection error occurs
     */
    public void releaseSender(String id, URI uri) throws BrokerException;

    /**
     * Connects to a receiver that dispatches invocation requests from an ZeroMQ XREP socket. The Invocation chain order is used to match an
     * invocation chain for dispatching the invocation.
     *
     * @param uri        the target service URI
     * @param chains     the invocation chains
     * @param metadata the ZeroMQ metadata to configure the underlying socket
     * @param loader     the classloader to load invocation parameters with
     * @throws BrokerException if a connection error occurs
     */
    public void connectToReceiver(URI uri, List<InvocationChain> chains, ZeroMQMetadata metadata, ClassLoader loader) throws BrokerException;

    /**
     * Releases previous connection to a receiver.
     *
     * @param uri the target service URI
     * @throws BrokerException if a connection error occurs
     */
    void releaseReceiver(URI uri) throws BrokerException;

    /**
     * Starts all senders and receivers.
     */
    void startAll();

    /**
     * Stops all senders and receivers.
     */
    void stopAll();

}
