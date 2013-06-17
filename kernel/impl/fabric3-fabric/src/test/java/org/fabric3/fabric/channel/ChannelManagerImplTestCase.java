/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 *
 */
public class ChannelManagerImplTestCase extends TestCase {
    private static final URI CHANNEL_URI = URI.create("test");
    private static final QName DEPLOYABLE = new QName("test", "test");

    private Channel channel;
    private ChannelManagerImpl manager;

    public void testDuplicateRegistration() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI).times(2);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER).times(2);
        EasyMock.replay(channel);

        manager.register(channel);
        try {
            manager.register(channel);
            fail();
        } catch (DuplicateChannelException e) {
            // expected
        }

        EasyMock.verify(channel);
    }

    public void testGetChannel() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER);
        EasyMock.replay(channel);

        manager.register(channel);
        assertEquals(channel, manager.getChannel(CHANNEL_URI, ChannelSide.CONSUMER));

        EasyMock.verify(channel);
    }

    public void testCount() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER);
        EasyMock.replay(channel);

        manager.register(channel);
        assertEquals(1, manager.getCount(CHANNEL_URI, ChannelSide.CONSUMER));
        assertEquals(-1, manager.getCount(CHANNEL_URI, ChannelSide.PRODUCER));
        manager.getAndIncrementChannel(CHANNEL_URI, ChannelSide.CONSUMER);
        assertEquals(2, manager.getCount(CHANNEL_URI, ChannelSide.CONSUMER));
        manager.getAndDecrementChannel(CHANNEL_URI, ChannelSide.CONSUMER);
        manager.getAndDecrementChannel(CHANNEL_URI, ChannelSide.CONSUMER);
        assertEquals(0, manager.getCount(CHANNEL_URI, ChannelSide.CONSUMER));

        EasyMock.verify(channel);
    }

    public void testUnRegister() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI).times(2);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER).times(2);
        EasyMock.replay(channel);

        manager.register(channel);
        manager.unregister(CHANNEL_URI, ChannelSide.CONSUMER);
        manager.register(channel);

        EasyMock.verify(channel);
    }

    public void testStartStopContext() throws Exception {
        EasyMock.expect(channel.getUri()).andReturn(CHANNEL_URI);
        EasyMock.expect(channel.getChannelSide()).andReturn(ChannelSide.CONSUMER);
        EasyMock.expect(channel.getDeployable()).andReturn(DEPLOYABLE).times(2);
        channel.start();
        channel.stop();
        EasyMock.replay(channel);

        manager.register(channel);
        manager.startContext(DEPLOYABLE);
        manager.stopContext(DEPLOYABLE);

        EasyMock.verify(channel);
    }

    public void setUp() throws Exception {
        super.setUp();
        manager = new ChannelManagerImpl();

        channel = EasyMock.createMock(Channel.class);
    }
}