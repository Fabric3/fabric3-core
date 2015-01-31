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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.net.URI;

import org.fabric3.api.host.ContainerException;

/**
 * Implements unit of work boundaries for a local transaction.
 */
public class LocalTransactionUnitOfWork implements UnitOfWork {
    private URI uri;
    private ContainerStatistics statistics;

    /**
     * Constructor.
     *
     * @param uri        the container URI this unit is associated with
     * @param statistics the JMS statistics tracker
     */
    public LocalTransactionUnitOfWork(URI uri, ContainerStatistics statistics) {
        this.uri = uri;
        this.statistics = statistics;
    }

    public void begin() {
        // do nothing
    }

    public void end(Session session, Message message) throws ContainerException {
        try {
            session.commit();
        } catch (JMSException e) {
            throw new ContainerException("Error handling message for " + uri, e);
        }
        statistics.incrementTransactions();
    }

    public void rollback(Session session) throws ContainerException {
        try {
            session.rollback();
        } catch (JMSException e) {
            throw new ContainerException("Error handling message for " + uri, e);
        }
        statistics.incrementTransactionsRolledBack();
    }

}
