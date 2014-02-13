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
package org.fabric3.fabric.container.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 * Default ChannelManager implementation.
 */
public class ChannelManagerImpl implements ChannelManager {
    private Map<URI, Holder> collocatedChannels = new ConcurrentHashMap<>();
    private Map<URI, Holder> producerChannels = new ConcurrentHashMap<>();
    private Map<URI, Holder> consumerChannels = new ConcurrentHashMap<>();

    public Channel getChannel(URI uri, ChannelSide channelSide) {
        checkUri(uri);
        Holder holder = getHolder(uri, channelSide);
        return holder != null ? holder.channel : null;
    }

    public Channel getAndIncrementChannel(URI uri, ChannelSide channelSide) {
        checkUri(uri);
        Holder holder = getHolder(uri, channelSide);
        if (holder == null) {
            return null;
        }
        holder.counter.incrementAndGet();
        return holder.channel;
    }

    public Channel getAndDecrementChannel(URI uri, ChannelSide channelSide) {
        checkUri(uri);
        Holder holder = getHolder(uri, channelSide);
        if (holder == null) {
            return null;
        }
        holder.counter.decrementAndGet();
        return holder.channel;
    }

    public int getCount(URI uri, ChannelSide channelSide) {
        checkUri(uri);
        Holder holder = getHolder(uri, channelSide);
        if (holder == null) {
            return -1;
        }
        return holder.counter.get();
    }

    public void register(Channel channel) throws DuplicateChannelException {
        ChannelSide channelSide = channel.getChannelSide();
        if (ChannelSide.COLLOCATED == channelSide) {
            checkAndPut(channel, collocatedChannels);
        } else if (ChannelSide.CONSUMER == channelSide) {
            checkAndPut(channel, consumerChannels);
        } else if (ChannelSide.PRODUCER == channelSide) {
            checkAndPut(channel, producerChannels);
        }
    }

    public Channel unregister(URI uri, ChannelSide channelSide) {
        Holder holder = null;
        switch (channelSide) {
            case CONSUMER:
                holder = consumerChannels.remove(uri);
                break;
            case PRODUCER:
                holder = producerChannels.remove(uri);
                break;
            case COLLOCATED:
                holder = collocatedChannels.remove(uri);
                break;
        }
        if (holder == null) {
            return null;
        }
        return holder.channel;
    }

    public void startContext(QName deployable) {
        doStart(deployable, collocatedChannels);
        doStart(deployable, consumerChannels);
        doStart(deployable, producerChannels);
    }

    public void stopContext(QName deployable) {
        doStop(deployable, producerChannels);
        doStop(deployable, consumerChannels);
        doStop(deployable, collocatedChannels);
    }

    private void checkUri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Channel URI was null");
        }
    }

    private Holder getHolder(URI uri, ChannelSide channelSide) {
        Holder holder = ChannelSide.CONSUMER == channelSide ? consumerChannels.get(uri) : producerChannels.get(uri);
        if (holder == null) {
            holder = collocatedChannels.get(uri);
        }
        return holder;
    }

    private void checkAndPut(Channel channel, Map<URI, Holder> map) throws DuplicateChannelException {
        URI uri = channel.getUri();
        if (map.containsKey(uri)) {
            throw new DuplicateChannelException("Channel already exists: " + uri);
        }
        map.put(uri, new Holder(channel));
    }

    private void doStart(QName deployable, Map<URI, Holder> map) {
        for (Holder holder : map.values()) {
            if (deployable.equals(holder.channel.getDeployable())) {
                holder.channel.start();
            }
        }
    }

    private void doStop(QName deployable, Map<URI, Holder> map) {
        for (Holder holder : map.values()) {
            if (deployable.equals(holder.channel.getDeployable())) {
                holder.channel.stop();
            }
        }
    }

    private class Holder {
        private Channel channel;
        private AtomicInteger counter = new AtomicInteger(1);

        private Holder(Channel channel) {
            this.channel = channel;
        }

    }
}
