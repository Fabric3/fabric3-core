/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.osoa.sca.annotations.Reference;
import org.zeromq.ZMQ;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.federation.AddressCache;
import org.fabric3.binding.zeromq.runtime.handler.AsyncFanOutHandler;
import org.fabric3.binding.zeromq.runtime.handler.DeserializingEventStreamHandler;
import org.fabric3.binding.zeromq.runtime.message.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.message.NonReliableSubscriber;
import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.spi.channel.ChannelConnection;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class ZeroMQBrokerImpl implements ZeroMQBroker {
    private ContextManager manager;
    private AddressCache addressCache;
    private ExecutorService executorService;
    private MessagingMonitor monitor;

    private Map<String, Subscriber> subscribers = new ConcurrentHashMap<String, Subscriber>();

    public ZeroMQBrokerImpl(@Reference ContextManager manager,
                            @Reference AddressCache addressCache,
                            @Reference ExecutorService executorService,
                            @Monitor MessagingMonitor monitor) {
        this.manager = manager;
        this.addressCache = addressCache;
        this.executorService = executorService;
        this.monitor = monitor;
    }

    public void subscribe(URI subscriberId, String channelName, ChannelConnection connection, ClassLoader loader) {
        Subscriber subscriber = subscribers.get(channelName);
        if (subscriber == null) {
            AsyncFanOutHandler fanOutHandler = new AsyncFanOutHandler(executorService);
            fanOutHandler.addConnection(subscriberId, connection);

            DeserializingEventStreamHandler head = new DeserializingEventStreamHandler(loader);
            head.setNext(fanOutHandler);

            List<SocketAddress> addresses = addressCache.getActiveAddresses(channelName);
            ZMQ.Context context = manager.getContext();
            subscriber = new NonReliableSubscriber(subscriberId.toString(), context, addresses, head, monitor);
            subscriber.start();
            addressCache.subscribe(subscriberId.toString(), subscriber);
            subscribers.put(channelName, subscriber);
        } else {
             subscriber.addConnection(subscriberId, connection);
        }
    }

    public void unsubscribe(URI subscriberId, String channelName) {
        Subscriber subscriber = subscribers.get(channelName);
        if (subscriber == null) {
            throw new IllegalStateException("MessageServer not found: " + subscriberId);
        }
        subscriber.removeConnection(subscriberId);
        if (!subscriber.hasConnections()){
            subscribers.remove(channelName);
            subscriber.stop();
        }
    }

    public Publisher getPublisher() {
        return null;
    }

    public void releasePublisher() {

    }
}
