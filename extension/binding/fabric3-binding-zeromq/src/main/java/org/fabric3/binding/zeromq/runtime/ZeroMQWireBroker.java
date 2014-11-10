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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
