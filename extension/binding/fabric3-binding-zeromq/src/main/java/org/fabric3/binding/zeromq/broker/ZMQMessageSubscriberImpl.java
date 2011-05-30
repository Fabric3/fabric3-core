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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessageListener;
import org.fabric3.binding.zeromq.runtime.ZMQMessageSubscriber;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

/**
 * The ZMQMessageSubscriber is responsible to connect to available
 * ZMQMessagePublishers.
 * 
 * When a subscriber is added a new thread is created with willl receive
 * messages through an inproc sub socket. The MessageSubscriber subscribes to a
 * possible remote Publisher and reveices messages. When a message is received
 * it will be forwarded to the inporc pub socket. This enables a unlimeted numer
 * of subscribes in a runtime. The MessageSubscriber will also be responsible to
 * reconnect to a different runtime if the connected runtime crashes.
 * 
 * @version $Revision$ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar
 *          2011) $
 * 
 */
public class ZMQMessageSubscriberImpl implements ZMQMessageSubscriber {
    private String                 channelName;
    private Context                context;
    private Socket                 mgmSocket;
    private List<MessageListener> listeners             = new ArrayList<MessageListener>();
    private ByteArrayInputStream   bis;
    private ThreadGroup            listenerGroup         = new ThreadGroup("msgListeners");
    private String                 mgmConnectionTemplate = "inproc://%sSubMgm";
    private ClassLoaderRegistry    classLoaderRegistry;

    protected ZMQBrokerMonitor     monitor;

    public ZMQMessageSubscriberImpl(Context context, ZeroMQMetadata metadata, ClassLoaderRegistry registry,
                                ZMQBrokerMonitor monitor) {
        this.channelName = metadata.getChannelName();
        this.monitor = monitor;
        this.context = context;
        mgmSocket = context.socket(ZMQ.PUB);
        String inproc = String.format(mgmConnectionTemplate, getChannelName());
        mgmSocket.bind(inproc);
        ZMQMessageReceiver receiver = new ZMQMessageReceiver(context, String.format("tcp://%s:%d", metadata.getHost(),
                metadata.getPort()));
        classLoaderRegistry = registry;
    }

    // first try to connect to the producer
    // how to find remote producers ?
    // this should be done by federation or by querying the runtimes zones
    public void init() {

    }

    public String getChannelName() {
        return channelName;
    }

    public void addSubscriber(MessageListener listener) {
        listeners.add(listener);

        Socket sock = context.socket(ZMQ.SUB);
        sock.connect("inproc://" + getChannelName() + "Publisher");
        sock.subscribe("".getBytes());
        MessageHandler handler = new MessageHandler(getChannelName(), sock, listener);
    }

    private class ZMQMessageReceiver extends Thread {
        private Context recvContext;
        private Socket  socket;
        private Poller  poller;
        private Socket  mgmSocket;
        private Socket  inprocPublisher;
        private boolean active = true;
        private String  connectionString;

        public ZMQMessageReceiver(Context context, String connectionString) {
            this.recvContext = context;
            connect(connectionString);
            this.start();
        }

        public void connect(String connectionString) {

            socket = recvContext.socket(ZMQ.SUB);
            this.mgmSocket = recvContext.socket(ZMQ.SUB);
            socket.subscribe("".getBytes());
            socket.connect(connectionString);
            monitor.connectedSubscriber(getChannelName(), connectionString);
            this.mgmSocket.connect(String.format(mgmConnectionTemplate, getChannelName()));
            inprocPublisher = recvContext.socket(ZMQ.PUB);
            inprocPublisher.bind("inproc://" + getChannelName() + "Publisher");
            poller = recvContext.poller(2);
            poller.register(socket, ZMQ.Poller.POLLIN);
            poller.register(mgmSocket, ZMQ.Poller.POLLIN);

        }

        public void run() {
            while (active) {
                if (poller.poll(250000) < 1)
                    continue;

                // for multipart support
                if (poller.pollin(0)) {
                    // notifyMessageListeners(socket.recv(0));
                    inprocPublisher.send(socket.recv(0), 0);
                }
                if (poller.pollin(1)) {
                    // deal with an internal mgm message
                    String msg = this.mgmSocket.recv(0).toString();
                    if (msg.equalsIgnoreCase("stop")) {
                        active = false;
                        // System.out.println("Oki doing shutdown");
                    }

                }
            }
        }
    }

    private class MessageHandler extends Thread {
        private MessageListener listener;
        private Socket           socket;

        public MessageHandler(String channel, Socket socket, MessageListener listener) {
            this.socket = socket;
            this.listener = listener;
            this.setDaemon(true);
            this.start();
        }

        public void run() {
            while (true) {
                byte[] msg = socket.recv(0);
                try {
                    bis = new ByteArrayInputStream(msg);
                    ObjectInput in = null;

                    in = new MultiClassLoaderObjectInputStream(bis, classLoaderRegistry);
                    Object o = in.readObject();
                    in.close();
                    bis.close();
                    this.listener.onMessage(o);
                } catch (IOException e) {
                    monitor.error(e);
                } catch (ClassNotFoundException e) {
                    monitor.error(e);
                }
            }
        }
    }

}
