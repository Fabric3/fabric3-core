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
package org.fabric3.channel.disruptor.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import org.fabric3.api.model.type.component.RingBufferData;
import org.fabric3.channel.disruptor.impl.RingBufferChannel;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.channel.ChannelBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Builds and disposes {@link RingBufferChannel}s.
 */
public class RingBufferChannelBuilder implements ChannelBuilder {
    private ExecutorService executorService;

    public RingBufferChannelBuilder(@Reference(name = "executorService") ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Channel build(PhysicalChannelDefinition definition) throws ContainerException {
        URI uri = definition.getUri();
        QName deployable = definition.getDeployable();

        RingBufferData data = definition.getMetadata(RingBufferData.class);
        int size = data.getRingSize();

        WaitStrategy strategy = createWaitStrategy(data);

        ChannelSide channelSide = definition.getChannelSide();

        return new RingBufferChannel(uri, deployable, size, strategy, channelSide, executorService);
    }

    public void dispose(PhysicalChannelDefinition definition, Channel channel) throws ContainerException {
        // no-op
    }

    private WaitStrategy createWaitStrategy(RingBufferData data) {
        switch (data.getWaitStrategy()) {
            case YIELDING:
                return new YieldingWaitStrategy();
            case SLEEPING:
                return new SleepingWaitStrategy();
            case BACKOFF:
                if (RingBufferData.PhasedBlockingType.LOCK == data.getPhasedBlockingType()) {
                    return PhasedBackoffWaitStrategy.withLock(data.getSpinTimeoutNanos(), data.getYieldTimeoutNanos(), TimeUnit.NANOSECONDS);
                } else {
                    return PhasedBackoffWaitStrategy.withSleep(data.getSpinTimeoutNanos(), data.getYieldTimeoutNanos(), TimeUnit.NANOSECONDS);
                }
            case SPIN:
                return new BusySpinWaitStrategy();
            case TIMEOUT:
                return new TimeoutBlockingWaitStrategy(data.getBlockingTimeoutNanos(), TimeUnit.NANOSECONDS);
            default:
                return new BlockingWaitStrategy();
        }
    }

}
