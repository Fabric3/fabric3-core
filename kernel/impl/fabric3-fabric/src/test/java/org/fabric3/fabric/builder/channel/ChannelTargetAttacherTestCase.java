/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.builder.channel;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
@EagerInit
public class ChannelTargetAttacherTestCase extends TestCase {

    public void testAttachDetach() throws Exception {
        URI channelUri = URI.create("channel");
        URI targetUri = URI.create("target");

        ChannelManager channelManager = EasyMock.createMock(ChannelManager.class);
        ChannelConnection connection = EasyMock.createMock(ChannelConnection.class);

        Channel channel = EasyMock.createMock((Channel.class));
        channel.attach(connection);
        EasyMock.expectLastCall();
        EasyMock.expect(channelManager.getChannel(channelUri)).andReturn(channel);

        EasyMock.replay(channelManager, connection, channel);
        
        ChannelTargetAttacher attacher = new ChannelTargetAttacher(channelManager);
        MockPhysicalDefinition source = new MockPhysicalDefinition(targetUri);
        ChannelTargetDefinition target = new ChannelTargetDefinition(channelUri);

        attacher.attach(source, target, connection);
        attacher.detach(source, target);

        EasyMock.verify(channelManager, connection, channel);
    }

    private class MockPhysicalDefinition extends PhysicalConnectionSourceDefinition {
        private static final long serialVersionUID = 9073512301309928102L;

        public MockPhysicalDefinition(URI uri) {
            setUri(uri);
        }
    }
}