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
package org.fabric3.channel.disruptor.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.channel.disruptor.common.RingBufferData;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class RingBufferChannelBuilderTestCase extends TestCase {
    public static final QName DEPLOYABLE = new QName("test", "test");
    public static final URI URI = java.net.URI.create("test");

    private RingBufferChannelBuilder builder;
    private PhysicalChannelDefinition definition;

    public void testBuild() throws Exception {
        Channel channel = builder.build(definition);

        assertEquals(URI, channel.getUri());
        assertEquals(DEPLOYABLE, channel.getDeployable());
    }

    public void testDispose() throws Exception {
        Channel channel = EasyMock.createMock(Channel.class);

        builder.dispose(definition, channel);
    }

    public void setUp() throws Exception {
        super.setUp();
        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);
        EasyMock.replay(executorService);

        builder = new RingBufferChannelBuilder(executorService);

        definition = new PhysicalChannelDefinition(URI, DEPLOYABLE, false, "ring.buffer", ChannelDeliveryType.ASYNCHRONOUS_WORKER);
        definition.setMetadata(new RingBufferData());
    }
}
