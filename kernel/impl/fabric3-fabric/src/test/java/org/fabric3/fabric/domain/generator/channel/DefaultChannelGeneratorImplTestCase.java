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
package org.fabric3.fabric.domain.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 *
 */
public class DefaultChannelGeneratorImplTestCase extends TestCase {
    private DefaultChannelGeneratorExtensionImpl generator = new DefaultChannelGeneratorExtensionImpl();

    public void testGenerate() throws Exception {
        Channel channelDefinition = new Channel("test");
        LogicalChannel channel = new LogicalChannel(URI.create("test"), channelDefinition, null);
        QName deployable = new QName("test", "test");
        channel.setDeployable(deployable);

        PhysicalChannelDefinition definition = generator.generate(channel, deployable);

        assertEquals(deployable, definition.getDeployable());
    }
}
