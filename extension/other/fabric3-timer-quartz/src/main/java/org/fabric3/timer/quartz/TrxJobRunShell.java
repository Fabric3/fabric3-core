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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * JobRunShell that wraps job invocations in a transaction.
 *
 * @version $Rev$ $Date$
 */
public class TrxJobRunShell extends F3JobRunShell {
    private TransactionManager tm;

    public TrxJobRunShell(JobRunShellFactory shellFactory, Scheduler scheduler, TransactionManager tm, SchedulingContext context) {
        super(shellFactory, scheduler, context);
        this.tm = tm;
    }

    protected void begin() throws SchedulerException {
        beginTransaction();
        super.begin();
    }

    protected void complete(boolean successfull) throws SchedulerException {
        super.complete(successfull);
        if (successfull) {
            commitTransaction();
        } else {
            rollbackTransaction();
        }
    }

    private void beginTransaction() throws SchedulerException {
        try {
            tm.begin();
        } catch (NotSupportedException e) {
            throw new SchedulerException(e);
        } catch (SystemException e) {
            throw new SchedulerException(e);
        }
    }

    private void commitTransaction() throws SchedulerException {
        try {
            if (tm.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                tm.commit();
            } else {
                tm.rollback();
            }
        } catch (SystemException e) {
            throw new SchedulerException(e);
        } catch (IllegalStateException e) {
            throw new SchedulerException(e);
        } catch (SecurityException e) {
            throw new SchedulerException(e);
        } catch (HeuristicMixedException e) {
            throw new SchedulerException(e);
        } catch (HeuristicRollbackException e) {
            throw new SchedulerException(e);
        } catch (RollbackException e) {
            throw new SchedulerException(e);
        }
    }

    private void rollbackTransaction() throws SchedulerException {
        try {
            tm.rollback();
        } catch (SystemException e) {
            throw new SchedulerException(e);
        }
    }

}
