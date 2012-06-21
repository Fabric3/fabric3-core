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
package org.fabric3.fabric.channel;

import java.io.Serializable;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.EventWrapper;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * @version $Rev: 8947 $ $Date: 2010-05-02 15:09:45 +0200 (Sun, 02 May 2010) $
 */
public class ReplicationHandlerTestCase extends TestCase {
    private ZoneTopologyService topologyService;
    private ReplicationMonitor monitor;
    private EventStreamHandler next;

    public void testReplicationSerializable() throws Exception {
        next.handle(EasyMock.notNull());
        topologyService.sendAsynchronous(EasyMock.eq("channel"), EasyMock.isA(MockEvent.class));
        EasyMock.replay(topologyService, monitor, next);

        ReplicationHandler handler = new ReplicationHandler("channel", topologyService, monitor);
        handler.setNext(next);

        Object event = new MockEvent();
        handler.handle(event);

        EasyMock.verify(topologyService, monitor, next);
    }

    public void testNoReplicationNotSerializable() throws Exception {
        next.handle(EasyMock.notNull());
        EasyMock.replay(topologyService, monitor, next);

        ReplicationHandler handler = new ReplicationHandler("channel", topologyService, monitor);
        handler.setNext(next);

        Object event = new Object();
        handler.handle(event);

        EasyMock.verify(topologyService, monitor, next);
    }

    public void testNoReplicationEventWrapper() throws Exception {
        next.handle(EasyMock.notNull());
        EasyMock.replay(topologyService, monitor, next);

        ReplicationHandler handler = new ReplicationHandler("channel", topologyService, monitor);
        handler.setNext(next);

        Object event = new MockEvent();
        EventWrapper wrapper = new EventWrapper(null, event);
        handler.handle(wrapper);

        EasyMock.verify(topologyService, monitor, next);
    }

    public void testOnNext() throws Exception {
        next.handle(EasyMock.notNull());
        EasyMock.replay(topologyService, monitor, next);

        ReplicationHandler handler = new ReplicationHandler("channel", topologyService, monitor);
        handler.setNext(next);

        Object event = new MockEvent();
        handler.onMessage(event);

        EasyMock.verify(topologyService, monitor, next);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void testReportReplicationError() throws Exception {
        next.handle(EasyMock.notNull());
        topologyService.sendAsynchronous(EasyMock.eq("channel"), EasyMock.isA(MockEvent.class));
        EasyMock.expectLastCall().andThrow(new MessageException("error"));

        monitor.error(EasyMock.isA(MessageException.class));
        EasyMock.replay(topologyService, monitor, next);

        ReplicationHandler handler = new ReplicationHandler("channel", topologyService, monitor);
        handler.setNext(next);

        Object event = new MockEvent();
        handler.handle(event);

        EasyMock.verify(topologyService, monitor, next);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        topologyService = EasyMock.createMock(ZoneTopologyService.class);
        monitor = EasyMock.createMock(ReplicationMonitor.class);
        next = EasyMock.createMock(EventStreamHandler.class);
    }

    private class MockEvent implements Serializable {
        private static final long serialVersionUID = 3194806347865372031L;
    }
}