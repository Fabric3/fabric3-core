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
package org.fabric3.timer.impl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Collects statistics for a timer pool.
 */
public class TimerPoolStatistics {
    private String poolName;
    private int coreSize;
    private long start;
    private AtomicInteger totalExecutions = new AtomicInteger();
    private AtomicLong totalExecutionTime = new AtomicLong();

    public TimerPoolStatistics(String poolName, int coreSize) {
        this.poolName = poolName;
        this.coreSize = coreSize;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    @ManagementOperation(description = "The timer pool name")
    public String getPoolName() {
        return poolName;
    }

    @ManagementOperation(description = "The number of threads allocated to the timer pool")
    public int getCoreSize() {
        return coreSize;
    }

    @ManagementOperation(description = "The approximate time in milliseconds the pool has been active")
    public long getActiveTime() {
        return System.currentTimeMillis() - start;
    }

    @ManagementOperation(description = "The total number of timer executions for the timer pool")
    public int getTotalExecutions() {
        return totalExecutions.get();
    }

    @ManagementOperation(description = "The total execution time in milliseconds for all timers in the pool")
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    @ManagementOperation(description = "The approximate average execution time in milliseconds for timers in the pool")
    public long getMeanExecutionTime() {
        if (totalExecutions.get() == 0) {
            return 0;
        }
        return totalExecutionTime.get() / totalExecutions.get();
    }

    public void incrementTotalExecutions() {
        totalExecutions.incrementAndGet();
    }

    public void incrementExecutionTime(long time) {
        totalExecutionTime.addAndGet(time);
    }

}