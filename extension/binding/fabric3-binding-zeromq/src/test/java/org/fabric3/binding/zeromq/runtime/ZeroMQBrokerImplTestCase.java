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
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.federation.AddressCache;
import org.fabric3.binding.zeromq.runtime.message.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.spi.channel.ChannelConnection;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class ZeroMQBrokerImplTestCase extends TestCase {
    private static final SocketAddress ADDRESS = new SocketAddress("runtime", "tcp", "10.10.10.1", 1061);

    private ContextManager manager;
    private AddressCache addressCache;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private ZMQ.Context context;
    private ChannelConnection connection;
    private ZeroMQBrokerImpl broker;


    public void testSubscribeUnsubscribe() throws Exception {
        addressCache.subscribe(EasyMock.eq("subscriber"), EasyMock.isA(Subscriber.class));
        EasyMock.expectLastCall();

        EasyMock.replay(context);
        EasyMock.replay(manager, addressCache, executorService, monitor, connection);

        broker.subscribe(URI.create("subscriber"), "endpoint", connection, getClass().getClassLoader());
        broker.unsubscribe(URI.create("subscriber"), "endpoint");

        EasyMock.verify(context);
        EasyMock.verify(manager, addressCache, executorService, monitor, connection);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = EasyMock.createMock(ZMQ.Context.class);
        manager = EasyMock.createMock(ContextManager.class);

        connection = EasyMock.createMock(ChannelConnection.class);

        EasyMock.expect(manager.getContext()).andReturn(context);

        addressCache = EasyMock.createMock(AddressCache.class);
        EasyMock.expect(addressCache.getActiveAddresses("endpoint")).andReturn(Collections.singletonList(ADDRESS));

        executorService = EasyMock.createMock(ExecutorService.class);
        monitor = EasyMock.createMock(MessagingMonitor.class);

        broker = new ZeroMQBrokerImpl(manager, addressCache, executorService, monitor);

    }
}
