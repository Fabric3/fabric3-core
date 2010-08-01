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
package org.fabric3.tx;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Enforces transaction policy for a wire operation.
 *
 * @version $Rev$ $Date$
 */
public class TxInterceptor implements Interceptor {
    private Interceptor next;
    private TransactionManager transactionManager;
    private TxAction txAction;
    private TxMonitor monitor;

    public TxInterceptor(TransactionManager transactionManager, TxAction txAction, TxMonitor monitor) {
        this.transactionManager = transactionManager;
        this.txAction = txAction;
        this.monitor = monitor;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message message) {

        Transaction transaction = getTransaction();

        if (txAction == TxAction.BEGIN) {
            if (transaction == null) {
                begin();
            }
        } else if (txAction == TxAction.SUSPEND && transaction != null) {
            suspend();
        }

        Message ret;
        try {
            ret = next.invoke(message);
        } catch (RuntimeException e) {
            if (txAction == TxAction.BEGIN && transaction == null) {
                rollback();
            } else if (txAction == TxAction.SUSPEND && transaction != null) {
                setRollbackOnly();
            }
            throw e;
        }

        if (txAction == TxAction.BEGIN && transaction == null && !ret.isFault()) {
            commit();
        } else if (txAction == TxAction.BEGIN && transaction == null && ret.isFault()) {
            rollback();
        } else if (txAction == TxAction.SUSPEND && transaction != null) {
            resume(transaction);
        }

        return ret;

    }

    private void setRollbackOnly() throws ServiceRuntimeException {
        try {
            monitor.markedForRollback(Thread.currentThread().getName());
            transactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private Transaction getTransaction() throws ServiceRuntimeException {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void rollback() throws ServiceRuntimeException {
        try {
            monitor.rolledback(Thread.currentThread().getName());
            transactionManager.rollback();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void begin() throws ServiceRuntimeException {
        try {
            transactionManager.begin();
        } catch (NotSupportedException e) {
            throw new ServiceRuntimeException(e);
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void suspend() throws ServiceRuntimeException {
        try {
            transactionManager.suspend();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void resume(Transaction transaction) throws ServiceRuntimeException {
        try {
            transactionManager.resume(transaction);
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        } catch (InvalidTransactionException e) {
            throw new ServiceRuntimeException(e);
        } catch (IllegalStateException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void commit() throws ServiceRuntimeException {
        try {
            if (transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                transactionManager.commit();
            } else {
                rollback();
            }
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        } catch (IllegalStateException e) {
            throw new ServiceRuntimeException(e);
        } catch (SecurityException e) {
            throw new ServiceRuntimeException(e);
        } catch (HeuristicMixedException e) {
            throw new ServiceRuntimeException(e);
        } catch (HeuristicRollbackException e) {
            throw new ServiceRuntimeException(e);
        } catch (RollbackException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
