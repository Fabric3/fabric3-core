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
package org.fabric3.fabric.container.builder.channel;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.channel.ChannelBuilder;
import org.fabric3.spi.container.builder.channel.ChannelBuilderRegistry;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.channel.RegistrationException;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelBuilderRegistryImpl implements ChannelBuilderRegistry {
    private ChannelManager channelManager;
    private Map<String, ChannelBuilder> builders = new HashMap<>();

    public ChannelBuilderRegistryImpl(@Reference ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Reference(required = false)
    public void setBuilders(Map<String, ChannelBuilder> builders) {
        this.builders = builders;
    }

    public Channel build(PhysicalChannelDefinition definition) throws BuilderException {
        URI uri = definition.getUri();
        ChannelSide channelSide = definition.getChannelSide();
        Channel channel = channelManager.getAndIncrementChannel(uri, channelSide);
        if (channel != null) {
            return channel;
        }
        ChannelBuilder builder = getBuilder(definition);
        channel = builder.build(definition);
        try {
            channelManager.register(channel);
            return channel;
        } catch (RegistrationException e) {
            throw new BuilderException(e);
        }
    }

    public void dispose(PhysicalChannelDefinition definition) throws BuilderException {
        ChannelBuilder builder = getBuilder(definition);
        try {
            URI uri = definition.getUri();
            ChannelSide channelSide = definition.getChannelSide();
            Channel channel = channelManager.getAndDecrementChannel(uri, channelSide);
            if (channelManager.getCount(uri, channelSide) == 0) {
                channelManager.unregister(uri, channelSide);
                builder.dispose(definition, channel);
            }

        } catch (RegistrationException e) {
            throw new BuilderException(e);
        }
    }

    private ChannelBuilder getBuilder(PhysicalChannelDefinition definition) throws BuilderException {
        ChannelBuilder builder = builders.get(definition.getType());
        if (builder == null) {
            throw new BuilderException("Channel builder not found for type " + definition.getType());
        }
        return builder;
    }

}
