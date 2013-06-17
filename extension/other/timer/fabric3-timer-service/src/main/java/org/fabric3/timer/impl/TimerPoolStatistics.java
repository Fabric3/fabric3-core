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