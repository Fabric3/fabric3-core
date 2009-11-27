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
package org.fabric3.timer.quartz;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Default implementation of a RunnableHolder.
 *
 * @version $Rev$ $Date$
 */
public class RunnableHolderImpl<T> extends FutureTask<T> implements RunnableHolder<T> {
    private String id;
    private QuartzTimerService timerService;

    public RunnableHolderImpl(String id, Runnable runnable, QuartzTimerService timerService) {
        super(runnable, null);
        this.id = id;
        this.timerService = timerService;
    }

    public String getId() {
        return id;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        boolean result = runAndReset();
        if (!result) {
            try {
                get();
            } catch (ExecutionException e) {
                // unwrap the exception
                JobExecutionException jex = new JobExecutionException(e.getCause());
                jex.setUnscheduleAllTriggers(true);  // unschedule the job
                throw jex;
            } catch (InterruptedException e) {
                JobExecutionException jex = new JobExecutionException(e);
                jex.setUnscheduleAllTriggers(true);  // unschedule the job
                throw jex;
            }
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            boolean val = super.cancel(mayInterruptIfRunning);
            // cancel against the timer service
            timerService.cancel(id);
            return val;
        } catch (SchedulerException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public long getDelay(TimeUnit unit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int compareTo(Delayed o) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
