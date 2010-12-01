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
package org.fabric3.binding.jms.runtime.container;

import java.net.URI;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.binding.jms.spi.common.TransactionType;


/**
 * Implements JMS transaction operations.
 *
 * @version $Rev$ $Date$
 */
@Management
public class TransactionHelper {
    private URI listenerUri;
    private TransactionType transactionType;
    private int transactionTimeout;
    private TransactionManager tm;

    private ContainerStatistics statistics;
    private MessageContainerMonitor monitor;


    /**
     * Constructor.
     *
     * @param listenerUri        the listener URI, typically a service or consumer
     * @param transactionType    the transaction type
     * @param transactionTimeout transaction timeout in seconds
     * @param tm                 the JTA transaction manager for transacted messaging
     * @param statistics         the JMS statistics tracker
     * @param monitor            the monitor
     */
    public TransactionHelper(URI listenerUri,
                             TransactionType transactionType,
                             int transactionTimeout,
                             TransactionManager tm,
                             ContainerStatistics statistics,
                             MessageContainerMonitor monitor) {
        this.listenerUri = listenerUri;
        this.transactionTimeout = transactionTimeout;
        this.transactionType = transactionType;
        this.tm = tm;
        this.statistics = statistics;
        this.monitor = monitor;
    }

    public void begin() throws TransactionException {
        try {
            tm.begin();
            tm.setTransactionTimeout(transactionTimeout);
        } catch (NotSupportedException e) {
            throw new TransactionException(e);
        } catch (SystemException e) {
            throw new TransactionException(e);
        }
    }

    public void rollback(Session session) throws TransactionException {
        if (TransactionType.GLOBAL == transactionType) {
            try {
                tm.rollback();
            } catch (SystemException e) {
                monitor.rollbackError(listenerUri, e);
            }
        } else {
            try {
                localRollback(session);
            } catch (JMSException e) {
                throw new TransactionException(e);
            }
        }
    }

    /**
     * Commits the current global (JTA) transaction.
     *
     * @throws TransactionException if a commit error was encountered
     */
    public void globalCommit() throws TransactionException {
        try {
            if (tm.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                tm.commit();
                statistics.incrementTransactions();
            } else {
                tm.rollback();
                statistics.incrementTransactionsRolledBack();
            }
        } catch (SystemException e) {
            throw new TransactionException("Error handling message for " + listenerUri, e);
        } catch (IllegalStateException e) {
            throw new TransactionException("Error handling message for " + listenerUri, e);
        } catch (SecurityException e) {
            throw new TransactionException("Error handling message for " + listenerUri, e);
        } catch (HeuristicMixedException e) {
            throw new TransactionException("Error handling message for " + listenerUri, e);
        } catch (HeuristicRollbackException e) {
            throw new TransactionException("Error handling message for " + listenerUri, e);
        } catch (RollbackException e) {
            throw new TransactionException("Error handling message for " + listenerUri, e);
        }
    }

    /**
     * Rollbacks the current global (JTA) transaction.
     *
     * @throws TransactionException if an error rolling back was encountered
     */
    public void globalRollback() throws TransactionException {
        try {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
                statistics.incrementTransactionsRolledBack();
            }
        } catch (SystemException e) {
            throw new TransactionException("Error reverting transaction for " + listenerUri, e);
        }
    }

    /**
     * Performs a local commit or acknowledgement if a local transaction or client acknowledgement is being used.
     *
     * @param session the session to commit
     * @param message the message to acknowledge
     * @throws JMSException if the commit fails
     */
    public void localCommitOrAcknowledge(Session session, Message message) throws JMSException {
        if (TransactionType.SESSION == transactionType) {
            session.commit();
            statistics.incrementTransactions();
        } else if (Session.CLIENT_ACKNOWLEDGE == session.getAcknowledgeMode()) {
            message.acknowledge();
        }
    }

    /**
     * Performs a local rollback of the JMS session.
     *
     * @param session the session to rollback
     * @throws JMSException if the rollback fails
     */
    public void localRollback(Session session) throws JMSException {
        if (TransactionType.SESSION == transactionType) {
            session.rollback();
            statistics.incrementTransactionsRolledBack();
        }
    }


}
