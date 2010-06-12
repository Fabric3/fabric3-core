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
package org.fabric3.timer.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executes scheduled tasks.
 *
 * @version $Rev$ $Date$
 */
public interface TimerService {
    String DEFAULT_POOL = "default";

    /**
     * Allocates a timer thread pool.
     *
     * @param poolName the pool name
     * @param coreSize the  thread pool size
     */
    void allocate(String poolName, int coreSize);

    /**
     * Shuts down and de-allocates a timer thread pool.
     *
     * @param poolName the pool name
     */
    void deallocate(String poolName);

    /**
     * Creates and executes a recurring action.
     *
     * @param poolName the timer thread pool to schedule the task with
     * @param task     the task to execute
     * @return a ScheduledFuture representing pending completion of the task and whose <tt>get()</tt> method will return <tt>null</tt> upon
     *         completion
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     */
    ScheduledFuture<?> scheduleRecurring(String poolName, Task task);

    /**
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     *
     * @param poolName the timer thread pool to schedule the task with
     * @param command  the task to execute
     * @param delay    the time from now to delay execution
     * @param unit     the time unit of the delay parameter
     * @return a ScheduledFuture representing pending completion of the task and whose <tt>get()</tt> method will return <tt>null</tt> upon
     *         completion
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     */
    public ScheduledFuture<?> schedule(String poolName, Runnable command, long delay, TimeUnit unit);

    /**
     * Creates and executes a ScheduledFuture that becomes enabled after the given delay.
     *
     * @param poolName the timer thread pool to schedule the task with
     * @param callable the function to execute
     * @param delay    the time from now to delay execution
     * @param unit     the time unit of the delay parameter
     * @return a ScheduledFuture that can be used to extract result or cancel
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if callable is null
     */
    public <V> ScheduledFuture<V> schedule(String poolName, Callable<V> callable, long delay, TimeUnit unit);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given period; that
     * is executions will commence after <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then <tt>initialDelay + 2 * period</tt>, and so on.
     * If any execution of the task encounters an exception, subsequent executions are suppressed. Otherwise, the task will only terminate via
     * cancellation or termination of the executor.  If any execution of this task takes longer than its period, then subsequent executions may start
     * late, but will not concurrently execute.
     *
     * @param poolName     the timer thread pool to schedule the task with
     * @param command      the task to execute
     * @param initialDelay the time to delay first execution
     * @param period       the period between successive executions
     * @param unit         the time unit of the initialDelay and period parameters
     * @return a ScheduledFuture representing pending completion of the task, and whose <tt>get()</tt> method will throw an exception upon
     *         cancellation
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if period less than or equal to zero
     */
    public ScheduledFuture<?> scheduleAtFixedRate(String poolName, Runnable command, long initialDelay, long period, TimeUnit unit);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given delay between
     * the termination of one execution and the commencement of the next.  If any execution of the task encounters an exception, subsequent executions
     * are suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.
     *
     * @param poolName     the timer thread pool to schedule the task with
     * @param command      the task to execute
     * @param initialDelay the time to delay first execution
     * @param delay        the delay between the termination of one execution and the commencement of the next
     * @param unit         the time unit of the initialDelay and delay parameters
     * @return a ScheduledFuture representing pending completion of the task, and whose <tt>get()</tt> method will throw an exception upon
     *         cancellation
     * @throws RejectedExecutionException if the task cannot be scheduled for execution
     * @throws NullPointerException       if command is null
     * @throws IllegalArgumentException   if delay less than or equal to zero
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(String poolName, Runnable command, long initialDelay, long delay, TimeUnit unit);


}
