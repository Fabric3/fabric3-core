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
package org.fabric3.monitor.impl.router;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.impl.common.MonitorConstants;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.monitor.spi.event.ParameterEntry;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Dispatches monitor events to destinations via a ring buffer. <p/> This implementation also supports synchronous dispatch. This mode should only be used in
 * development to avoid startup time associated with pre-allocating ring buffer entries.
 */
public class RingBufferDestinationRouterImpl implements RingBufferDestinationRouter {
    public static final String ASYNCHRONOUS_MODE = "asynchronous";
    private static final String SYNCHRONOUS_MODE = "synchronous";

    private ExecutorService executorService;
    private MonitorDestinationRegistry registry;
    private DestinationMonitor monitor;

    private Disruptor<MonitorEventEntry> disruptor;

    private int capacity = MonitorConstants.DEFAULT_BUFFER_CAPACITY;
    private int ringSize = 65536;
    private String strategyType = "blocking";
    private long blockingTimeoutNanos = 1000;
    private long spinTimeoutNanos = 1000;
    private long yieldTimeoutNanos = 1000;
    private String phasedBlockingType = "lock";
    private boolean enabled = false;  // true if the ring buffer (asynchronous mode) is enabled

    public RingBufferDestinationRouterImpl(@Reference(name = "executorService") ExecutorService executorService,
                                           @Reference MonitorDestinationRegistry registry,
                                           @Monitor DestinationMonitor monitor) {
        this.executorService = executorService;
        this.registry = registry;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Property(required = false)
    public void setRingSize(int ringSize) {
        this.ringSize = ringSize;
    }

    @Property(required = false)
    public void setMode(String mode) {
        if (ASYNCHRONOUS_MODE.equalsIgnoreCase(mode)) {
            this.enabled = true;
        } else if (SYNCHRONOUS_MODE.equalsIgnoreCase(mode)) {
            this.enabled = false;
        } else {
            this.enabled = false;
            monitor.unknownMode(mode);
        }
    }

    @Property(required = false)
    public void setBlockingTimeoutNanos(long blockingTimeoutNanos) {
        this.blockingTimeoutNanos = blockingTimeoutNanos;
    }

    @Property(required = false)
    public void setPhasedBlockingType(String type) {
        this.phasedBlockingType = type;
    }

    @Property(required = false)
    public void setWaitStrategy(String strategy) {
        strategyType = strategy;
    }

    @Property(required = false)
    public void setSpinTimeoutNanos(long timeout) {
        this.spinTimeoutNanos = timeout;
    }

    @Property(required = false)
    public void setYieldTimeoutNanos(long timeout) {
        this.yieldTimeoutNanos = timeout;
    }

    @Init
    public void init() throws FileNotFoundException {
        if (!enabled) {
            return;
        }
        WaitStrategy waitStrategy = createWaitStrategy();
        MonitorEventEntryFactory factory = new MonitorEventEntryFactory(capacity);
        disruptor = new Disruptor<>(factory, ringSize, executorService, ProducerType.MULTI, waitStrategy);
        MonitorEventHandler handler = new MonitorEventHandler(registry);
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    @Destroy
    public void destroy() throws IOException {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    public int getDestinationIndex(String name) {
        return registry.getIndex(name);
    }

    public MonitorEventEntry get() {
        RingBuffer<MonitorEventEntry> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        MonitorEventEntry entry = ringBuffer.get(sequence);

        entry.getBuffer().clear();
        for (ParameterEntry parameterEntry : entry.getEntries()) {
            parameterEntry.reset();
        }
        entry.setSequence(sequence);

        return entry;
    }

    public void publish(MonitorEventEntry entry) {
        disruptor.getRingBuffer().publish(entry.getSequence());
    }

    public void send(MonitorLevel level, int destinationIndex, long timestamp, String source, String template, boolean parse, Object... args) {
        // Note this method is not garbage-free as primitives will be auto-boxed. This method should only be called to flush bootstrap events or when the
        // ring buffer is disabled.
        if (enabled) {
            MonitorEventEntry entry = null;
            try {
                entry = get();
                entry.setDestinationIndex(destinationIndex);
                entry.setTimestampNanos(System.nanoTime());
                entry.setLevel(level);
                entry.setEntryTimestamp(timestamp);
                entry.setTemplate(template);
                entry.setParse(parse);
                entry.setLimit(args == null ? 0 : args.length);
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        Object arg = args[i];
                        entry.getEntries()[i].setObjectValue(arg);
                    }
                }
            } finally {
                if (entry != null) {
                    publish(entry);
                }
            }
        } else {
            // synchronize the write
            synchronized (this) {
                try {
                    registry.write(destinationIndex, level, timestamp, source, template, args);
                } catch (IOException e) {
                    throw new ServiceRuntimeException(e);
                }
            }
        }
    }

    private WaitStrategy createWaitStrategy() {
        WaitStrategy waitStrategy;
        if ("blocking".equalsIgnoreCase(strategyType)) {
            waitStrategy = new BlockingWaitStrategy();
            monitor.blockingStrategy();
        } else if ("yielding".equalsIgnoreCase(strategyType)) {
            waitStrategy = new YieldingWaitStrategy();
            monitor.yieldingStrategy();
        } else if ("sleeping".equalsIgnoreCase(strategyType)) {
            waitStrategy = new SleepingWaitStrategy();
            monitor.sleepingStrategy();
        } else if ("backoff".equalsIgnoreCase(strategyType)) {
            if ("lock".equalsIgnoreCase(phasedBlockingType)) {
                waitStrategy = PhasedBackoffWaitStrategy.withLock(spinTimeoutNanos, yieldTimeoutNanos, TimeUnit.NANOSECONDS);
                monitor.phasedBackoffWithLockStrategy(spinTimeoutNanos, yieldTimeoutNanos);
            } else {
                waitStrategy = PhasedBackoffWaitStrategy.withSleep(spinTimeoutNanos, yieldTimeoutNanos, TimeUnit.NANOSECONDS);
                monitor.phasedBackoffWithSleepStrategy(spinTimeoutNanos, yieldTimeoutNanos);
            }
        } else if ("spin".equalsIgnoreCase(strategyType)) {
            waitStrategy = new BusySpinWaitStrategy();
            monitor.busySpinStrategy();
        } else if ("timeout".equalsIgnoreCase(strategyType)) {
            waitStrategy = new TimeoutBlockingWaitStrategy(blockingTimeoutNanos, TimeUnit.NANOSECONDS);
            monitor.timeoutStrategy(blockingTimeoutNanos);
        } else {
            waitStrategy = new BlockingWaitStrategy();
            monitor.invalidStrategy(strategyType);
        }
        return waitStrategy;
    }

}
