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

    public void begin() throws WorkException {
        try {
            tm.begin();
            tm.setTransactionTimeout(transactionTimeout);
        } catch (NotSupportedException | SystemException e) {
            throw new WorkException(e);
        }
    }

    public void end(Session session, Message message) throws WorkException {
        try {
            if (tm.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                tm.commit();
                statistics.incrementTransactions();
            } else {
                tm.rollback();
                statistics.incrementTransactionsRolledBack();
            }
        } catch (SystemException | RollbackException | HeuristicRollbackException | HeuristicMixedException | SecurityException | IllegalStateException e) {
            throw new WorkException("Error handling message for " + uri, e);
        }
    }

    public void rollback(Session session) throws WorkException {
        try {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
                statistics.incrementTransactionsRolledBack();
            }
        } catch (SystemException e) {
            throw new WorkException("Error reverting transaction for " + uri, e);
        }
    }

}
