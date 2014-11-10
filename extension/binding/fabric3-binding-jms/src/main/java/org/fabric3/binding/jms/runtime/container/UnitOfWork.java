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

/**
 * Implements unit of work boundaries for a JMS operation. Implementations support JTA transactions, local transactions, session auto-acknowledge, and client
 * acknowledge.
 */
public interface UnitOfWork {

    /**
     * Begins a unit of work.
     *
     * @throws WorkException if there is an error beginning a global transaction.
     */
    void begin() throws WorkException;

    /**
     * Commits the unit of work.
     *
     * @param session the session the work is associated with
     * @param message the message the work is associated with
     * @throws WorkException
     */
    void end(Session session, Message message) throws WorkException;

    /**
     * Aborts the unit of work.
     *
     * @param session the current JMS session the transaction is associated with
     * @throws WorkException if there is a rollback error
     */
    void rollback(Session session) throws WorkException;

}
