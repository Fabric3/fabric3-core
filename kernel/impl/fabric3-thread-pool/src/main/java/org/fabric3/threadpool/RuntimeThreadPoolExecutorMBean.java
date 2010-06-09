/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.threadpool;

import org.fabric3.api.annotation.management.Management;

/**
 * MBean interface for the runtime thread pool executor.
 *
 * @version $Rev$ $Date$
 */
@Management
public interface RuntimeThreadPoolExecutorMBean {

    /**
     * Returns the time a thread can be processing work before it is considered stalled.
     *
     * @return the time a thread can be processing work before it is considered stalled
     */
    int getStallThreshold();

    /**
     * Sets the time a thread can be processing work before it is considered stalled. The default is ten minutes.
     *
     * @param stallThreshold the time a thread can be processing work before it is considered stalled
     */
    void setStallThreshold(int stallThreshold);

    /**
     * Returns the approximate number of threads actively executing tasks.
     *
     * @return the approximate number of threads actively executing tasks
     */
    int getActiveCount();

    /**
     * Returns the maximum thread pool size.
     *
     * @return the maximum thread pool size
     */
    int getMaximumPoolSize();

    /**
     * Sets the maximum thread pool size.
     *
     * @param size the maximum thread pool size
     */
    void setMaximumPoolSize(int size);

    /**
     * Returns the core thread pool size.
     *
     * @return the core thread pool size
     */
    int getCorePoolSize();

    /**
     * Sets the core thread pool size.
     *
     * @param size the core thread pool size
     */
    void setCorePoolSize(int size);

    /**
     * Returns the largest size the thread pool reached.
     *
     * @return the largest size the thread pool reached
     */
    int getLargestPoolSize();

    /**
     * Returns the remaining capacity the receive queue has before additional work will be rejected.
     *
     * @return the remaining receive queue capacity
     */
    int getRemainingCapacity();

    /**
     * Returns the total time the thread pool has spent executing requests.
     *
     * @return the total time the thread pool has spent executing requests
     */
    long getTotalExecutionTime();

    /**
     * Returns the average elapsed time to process a work request.
     *
     * @return the average elapsed time to process a work request
     */
    double getMeanExecutionTime();

    /**
     * Returns the total number of work items processed by the thread pool.
     *
     * @return the total number of work items processed by the thread pool
     */
    long getCompletedWorkCount();

    /**
     * Returns the longest elapsed time for a currently running work request.
     *
     * @return the longest elapsed time for a currently running work request
     */
    long getLongestRunning();

}