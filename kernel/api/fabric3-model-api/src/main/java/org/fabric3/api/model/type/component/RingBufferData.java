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
package org.fabric3.api.model.type.component;

import java.io.Serializable;

/**
 *
 */
public class RingBufferData implements Serializable {
    private static final long serialVersionUID = 37966413953268128L;

    public enum WaitStrategyType {
        BLOCKING, YIELDING, SLEEPING, BACKOFF, SPIN, TIMEOUT
    }

    public enum PhasedBlockingType {
        LOCK, SLEEP
    }

    private int ringSize;
    private long blockingTimeoutNanos = 1000;
    private long spinTimeoutNanos = 1000;
    private long yieldTimeoutNanos = 1000;
    private PhasedBlockingType phasedBlockingType = PhasedBlockingType.LOCK;

    private WaitStrategyType waitStrategy = WaitStrategyType.BLOCKING;

    public void setRingSize(int ringSize) {
        this.ringSize = ringSize;
    }

    public int getRingSize() {
        return ringSize;
    }

    public long getBlockingTimeoutNanos() {
        return blockingTimeoutNanos;
    }

    public void setBlockingTimeoutNanos(long blockingTimeoutNanos) {
        this.blockingTimeoutNanos = blockingTimeoutNanos;
    }

    public long getSpinTimeoutNanos() {
        return spinTimeoutNanos;
    }

    public void setSpinTimeoutNanos(long spinTimeoutNanos) {
        this.spinTimeoutNanos = spinTimeoutNanos;
    }

    public long getYieldTimeoutNanos() {
        return yieldTimeoutNanos;
    }

    public void setYieldTimeoutNanos(long yieldTimeoutNanos) {
        this.yieldTimeoutNanos = yieldTimeoutNanos;
    }

    public PhasedBlockingType getPhasedBlockingType() {
        return phasedBlockingType;
    }

    public void setPhasedBlockingType(PhasedBlockingType phasedBlockingType) {
        this.phasedBlockingType = phasedBlockingType;
    }

    public WaitStrategyType getWaitStrategy() {
        return waitStrategy;
    }

    public void setWaitStrategy(WaitStrategyType waitStrategy) {
        this.waitStrategy = waitStrategy;
    }
}
