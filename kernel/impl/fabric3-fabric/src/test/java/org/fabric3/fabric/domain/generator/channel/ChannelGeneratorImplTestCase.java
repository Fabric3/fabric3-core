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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.ChannelGeneratorExtension;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 *
 */
public class ChannelGeneratorImplTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("foo", "bar");

    public void testGenerateLocalChannelBuild() throws Exception {

        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        EasyMock.replay(registry, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl();
        generator.extensions = Collections.singletonMap("default", channelGenerator);
        LogicalChannel channel = createChannel();
        PhysicalChannel physicalChannel = generator.generate(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(physicalChannel);

        EasyMock.verify(registry, channelGenerator);
    }

    public void testGenerateLocalChannelFullBuild() throws Exception {
        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        EasyMock.replay(registry, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl();
        generator.extensions = Collections.singletonMap("default", channelGenerator);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.PROVISIONED);
        PhysicalChannel physicalChannel = generator.generate(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(physicalChannel);

        EasyMock.verify(registry, channelGenerator);
    }

    public void testGenerateLocalChannelDispose() throws Exception {
        ChannelGeneratorExtension channelGenerator = getChannelGenerator();

        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);

        EasyMock.replay(registry, channelGenerator);

        ChannelGeneratorImpl generator = new ChannelGeneratorImpl();
        generator.extensions = Collections.singletonMap("default", channelGenerator);
        LogicalChannel channel = createChannel();
        channel.setState(LogicalState.MARKED);
        PhysicalChannel physicalChannel = generator.generate(channel, DEPLOYABLE, ChannelDirection.CONSUMER);

        assertNotNull(physicalChannel);

        EasyMock.verify(registry, channelGenerator);
    }

    private ChannelGeneratorExtension getChannelGenerator() throws Fabric3Exception {
        ChannelGeneratorExtension channelGenerator = EasyMock.createMock(ChannelGeneratorExtension.class);
        PhysicalChannel physicalChannel = new PhysicalChannel(URI.create("channel"), new QName("test", "test"));
        EasyMock.expect(channelGenerator.generate(EasyMock.isA(LogicalChannel.class), EasyMock.eq(DEPLOYABLE))).andReturn(physicalChannel);
        return channelGenerator;
    }

    private LogicalChannel createChannel() {
        Channel definition = new Channel("channel");
        return new LogicalChannel(URI.create("channel"), definition, null);
    }

}