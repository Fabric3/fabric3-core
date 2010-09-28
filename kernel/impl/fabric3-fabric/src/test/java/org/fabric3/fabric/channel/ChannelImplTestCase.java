/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.PassThroughHandler;

/**
 * The default Channel implementation.
 *
 * @version $Rev$ $Date$
 */
public class ChannelImplTestCase extends TestCase {
    private ChannelImpl channel;
    private FanOutHandler fanOutHandler;

    public void testAddRemove() throws Exception {
        BlockingHandler handler = new BlockingHandler();
        BlockingHandler handler2 = new BlockingHandler();
        channel.addHandler(handler);
        channel.addHandler(handler2);

        EventStreamHandler head = new PassThroughHandler();
        channel.attach(head);

        channel.removeHandler(handler);
        handler.setClosed(true);

        head.handle(new Object());

        channel.removeHandler(handler2);
        handler2.setClosed(true);

        head.handle(new Object());
        EasyMock.verify(fanOutHandler);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fanOutHandler = EasyMock.createMock(FanOutHandler.class);
        fanOutHandler.handle(EasyMock.notNull());
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(fanOutHandler);
        channel = new ChannelImpl(URI.create("channel"), new QName("test", "test"), fanOutHandler);
    }

    private class BlockingHandler extends PassThroughHandler {
        private boolean closed;

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        @Override
        public void handle(Object event) {
            if (closed) {
                fail("Handler not properly removed");
            }
            super.handle(event);
        }
    }
}