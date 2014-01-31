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
package org.fabric3.channel.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class DefaultChannelBuilderTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testBuildChannel() throws Exception {
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(URI.create("test"), new QName("foo", "bar"));
        definition.setBindingDefinition(new MockBindingDefinition());

        Channel channel = EasyMock.createMock(Channel.class);

        ChannelBindingBuilder bindingBuilder = EasyMock.createMock(ChannelBindingBuilder.class);
        bindingBuilder.build(EasyMock.isA(PhysicalChannelBindingDefinition.class), EasyMock.isA(Channel.class));

        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);

        EasyMock.replay(channel, bindingBuilder);

        DefaultChannelBuilder builder = new DefaultChannelBuilder(executorService);

        Map bindingBuilderMap = Collections.singletonMap(MockBindingDefinition.class, bindingBuilder);
        builder.setBindingBuilders(bindingBuilderMap);

        assertNotNull(builder.build(definition));

        EasyMock.verify(channel, bindingBuilder);
    }

    private class MockBindingDefinition extends PhysicalChannelBindingDefinition {
        private static final long serialVersionUID = -474926224717103363L;

        private MockBindingDefinition() {
            super(ChannelDeliveryType.DEFAULT);
        }
    }
}
