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
package org.fabric3.fabric.container.channel;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 * Default ChannelManager implementation.
 */
public class ChannelManagerImpl implements ChannelManager {
    private Map<URI, Holder> collocatedChannels = new ConcurrentHashMap<>();
    private Map<URI, Holder> producerChannels = new ConcurrentHashMap<>();
    private Map<URI, Holder> consumerChannels = new ConcurrentHashMap<>();
    private boolean started;

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

    public void register(Channel channel) throws Fabric3Exception {
        if (started) {
            channel.start();
        }
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

    public void startContext(URI uri) {
        doStart(uri, collocatedChannels);
        doStart(uri, consumerChannels);
        doStart(uri, producerChannels);
    }

    public void stopContext(URI uri) {
        doStop(uri, producerChannels);
        doStop(uri, consumerChannels);
        doStop(uri, collocatedChannels);
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

    private void checkAndPut(Channel channel, Map<URI, Holder> map) throws Fabric3Exception {
        URI uri = channel.getUri();
        if (map.put(uri, new Holder(channel)) != null) {
            throw new Fabric3Exception("Channel already exists: " + uri);
        }
    }

    private void doStart(URI uri, Map<URI, Holder> map) {
        started = true;
        map.values().stream().filter(holder -> uri.equals(holder.channel.getContributionUri())).forEach(holder -> holder.channel.start());
    }

    private void doStop(URI uri, Map<URI, Holder> map) {
        map.values().stream().filter(holder -> uri.equals(holder.channel.getContributionUri())).forEach(holder -> holder.channel.stop());
        started = false;
    }

    private class Holder {
        private Channel channel;
        private AtomicInteger counter = new AtomicInteger(1);

        private Holder(Channel channel) {
            this.channel = channel;
        }

    }
}
