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
package org.fabric3.binding.jms.runtime.container;

import javax.jms.Message;
import javax.jms.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Implements unit of work boundaries for a JMS operation.
 */
public class JtaUnitOfWork implements UnitOfWork {
    private URI uri;
    private int transactionTimeout;
    private TransactionManager tm;

    private ContainerStatistics statistics;

    /**
     * Constructor.
     *
     * @param uri        the container URI this unit is associated with
     * @param timeout    transaction timeout in seconds
     * @param tm         the JTA transaction manager for transacted messaging
     * @param statistics the JMS statistics tracker
     */
    public JtaUnitOfWork(URI uri, int timeout, TransactionManager tm, ContainerStatistics statistics) {
        this.uri = uri;
        this.transactionTimeout = timeout;
        this.tm = tm;
        this.statistics = statistics;
    }

    public void begin() throws Fabric3Exception {
        try {
            tm.begin();
            tm.setTransactionTimeout(transactionTimeout);
        } catch (NotSupportedException | SystemException e) {
            throw new Fabric3Exception(e);
        }
    }

    public void end(Session session, Message message) throws Fabric3Exception {
        try {
            if (tm.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                tm.commit();
                statistics.incrementTransactions();
            } else {
                tm.rollback();
                statistics.incrementTransactionsRolledBack();
            }
        } catch (SystemException | RollbackException | HeuristicRollbackException | HeuristicMixedException | SecurityException | IllegalStateException e) {
            throw new Fabric3Exception("Error handling message for " + uri, e);
        }
    }

    public void rollback(Session session) throws Fabric3Exception {
        try {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
                statistics.incrementTransactionsRolledBack();
            }
        } catch (SystemException e) {
            throw new Fabric3Exception("Error reverting transaction for " + uri, e);
        }
    }

}
