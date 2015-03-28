/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.channel.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 *
 */
public class DefaultChannelBuilderTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testBuildChannel() throws Exception {
        PhysicalChannel physicalChannel = new PhysicalChannel(URI.create("test"), new QName("foo", "bar"));

        Channel channel = EasyMock.createMock(Channel.class);

        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);

        EasyMock.replay(channel);

        DefaultChannelBuilder builder = new DefaultChannelBuilder(executorService);

        assertNotNull(builder.build(physicalChannel));

        EasyMock.verify(channel);
    }

}
