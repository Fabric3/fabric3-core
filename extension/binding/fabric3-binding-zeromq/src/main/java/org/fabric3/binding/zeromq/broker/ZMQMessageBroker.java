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
package org.fabric3.binding.zeromq.broker;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.scope.Scopes;
import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.IMessageListener;
import org.fabric3.binding.zeromq.runtime.IZMQMessageBroker;
import org.fabric3.binding.zeromq.runtime.IZMQMessagePublisher;
import org.fabric3.binding.zeromq.runtime.IZMQMessageSubscriber;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

/**
 * The ZMQMessageBroker manages the IZMQMessagePublisher and
 * IZMQMessageSubscriber instances. There is one IZMQMessagePublisher per
 * runtime and channel. All producers for the same channel will send messages
 * through the same IZMQMessagePublisher instance. There is one
 * IZMQMessageSubscriber per runtime and channel. All consumers for the same
 * channel will receive messages through the same IZMQMessageSubscriber
 * instance.
 * 
 * @version $Revision$ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar
 *          2011) $
 * 
 */
@EagerInit
@Scope(Scopes.COMPOSITE)
@Management(description = "ZeroMQ Broker instance", group = "binding", name = "zmq", path = "/runtime/binding/zmq")
public class ZMQMessageBroker implements IZMQMessageBroker {

    private Map<String, IZMQMessagePublisher>  publishers  = new HashMap<String, IZMQMessagePublisher>();
    private Map<String, IZMQMessageSubscriber> subscribers = new HashMap<String, IZMQMessageSubscriber>();
    private Context                            context;
    private String                             zmqLibraryPath;

    @Reference
    protected PortAllocator                    allocator;

    @Reference
    protected ClassLoaderRegistry              classLoaderReistry;

    @Monitor
    protected ZMQBrokerMonitor                 monitor;

    public ZMQMessageBroker() {

    }

    @Property
    public void setZmqLibraryPath(String path) {
        this.zmqLibraryPath = File.pathSeparator + path;
        monitor.addedZMQLibraryPath(path);
    }

    @Init
    protected void init() {
        String syspath = System.getProperty("java.library.path");
        // syspath = syspath + ":/home/jb/dev/zmq/zmq-standard/lib";
        syspath += zmqLibraryPath;
        System.setProperty("java.library.path", syspath);
        try {
            Field fieldsyspath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldsyspath.setAccessible(true);
            fieldsyspath.set(null, null);
            context = ZMQ.context(1);
        } catch (Exception e) {
            monitor.error(e);
        }
    }

    @ManagementOperation(path = "/")
    public String getChannels() {
        return "seas";
    }

    @ManagementOperation(description = "Get all registered publishers", path = "publishers")
    public List<String> getPublishers() {
        List<String> pubs = new ArrayList<String>();
        pubs.addAll(publishers.keySet());
        return pubs;
    }

    @Override
    public IZMQMessagePublisher createPublisher(ZeroMQMetadata metadata) {
        IZMQMessagePublisher publisher = publishers.get(metadata.getChannelName());
        if (publisher == null) {
            allocatePort(metadata);
            publisher = new ZMQMessagePublisher(context, metadata, monitor);
            publishers.put(metadata.getChannelName(), publisher);

        }
        return publisher;
    }

    protected void allocatePort(ZeroMQMetadata metadata) {
        allocatePort(metadata, false);
    }

    protected void allocatePort(ZeroMQMetadata metadata, boolean force) {
        try {
            if (metadata.getPort() == ZeroMQMetadata.PORT_NOT_SET || force) {
                int port = allocator.allocate(metadata.getChannelName(), IZMQMessageBroker.ALLOCATOR_TYPE_ZMQ_PUB);
                metadata.setPort(port);
            }
            allocator.reserve(metadata.getChannelName(), ALLOCATOR_TYPE_ZMQ_PUB, metadata.getPort());
        } catch (PortAllocationException e) {
            monitor.error(e);
        }

    }

    @Override
    public void addSubscriber(IMessageListener listener, ZeroMQMetadata metadata) {
        IZMQMessageSubscriber subscriber = subscribers.get(metadata.getChannelName());
        if (subscriber == null) {
            subscriber = new ZMQMessageSubscriber(context, metadata, classLoaderReistry, monitor);
            subscribers.put(subscriber.getChannelName(), subscriber);
        }
        subscriber.addSubscriber(listener);
    }

}
