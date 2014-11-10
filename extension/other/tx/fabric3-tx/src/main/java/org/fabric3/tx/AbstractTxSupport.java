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

/**
 * Transaction functionality common to interceptors and event stream handlers.
 */
public abstract class AbstractTxSupport {
    protected TransactionManager transactionManager;
    protected TxAction txAction;
    protected TxMonitor monitor;

    public AbstractTxSupport(TransactionManager transactionManager, TxAction txAction, TxMonitor monitor) {
        this.transactionManager = transactionManager;
        this.txAction = txAction;
        this.monitor = monitor;
    }

    protected void setRollbackOnly() throws ServiceRuntimeException {
        try {
            monitor.markedForRollback(Thread.currentThread().getName());
            transactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected Transaction getTransaction() throws ServiceRuntimeException {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected void rollback() throws ServiceRuntimeException {
        try {
            monitor.rolledback(Thread.currentThread().getName());
            transactionManager.rollback();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected void begin() throws ServiceRuntimeException {
        try {
            transactionManager.begin();
        } catch (NotSupportedException | SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected void suspend() throws ServiceRuntimeException {
        try {
            transactionManager.suspend();
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected void resume(Transaction transaction) throws ServiceRuntimeException {
        try {
            transactionManager.resume(transaction);
        } catch (SystemException | IllegalStateException | InvalidTransactionException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    protected void commit() throws ServiceRuntimeException {
        try {
            if (transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                transactionManager.commit();
            } else {
                rollback();
            }
        } catch (SystemException | RollbackException | HeuristicRollbackException | HeuristicMixedException | SecurityException | IllegalStateException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
