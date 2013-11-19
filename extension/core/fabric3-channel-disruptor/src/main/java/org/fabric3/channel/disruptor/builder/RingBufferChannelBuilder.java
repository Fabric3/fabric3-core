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
import org.fabric3.spi.container.builder.BuilderException;
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

    public RingBufferChannelBuilder(@Reference ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Channel build(PhysicalChannelDefinition definition) throws BuilderException {
        URI uri = definition.getUri();
        QName deployable = definition.getDeployable();

        RingBufferData data = definition.getMetadata(RingBufferData.class);
        int size = data.getRingSize();

        WaitStrategy strategy = createWaitStrategy(data);

        ChannelSide channelSide = definition.getChannelSide();

        return new RingBufferChannel(uri, deployable, size, strategy, channelSide, executorService);
    }

    public void dispose(PhysicalChannelDefinition definition, Channel channel) throws BuilderException {
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
