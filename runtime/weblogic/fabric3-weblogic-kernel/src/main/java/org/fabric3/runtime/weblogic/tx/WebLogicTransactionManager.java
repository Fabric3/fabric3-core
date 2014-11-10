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
package org.fabric3.runtime.weblogic.tx;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;

/**
 * Implementation that delegates to the WebLogic TransactionManager.
 */
@EagerInit
public class WebLogicTransactionManager implements TransactionManager {
    private TransactionManager delegate;

    @Init
    public void init() throws NamingException {
        InitialContext ctx = new InitialContext();
        delegate = (TransactionManager) ctx.lookup("javax.transaction.TransactionManager");
    }

    public void begin() throws NotSupportedException, SystemException {
        delegate.begin();
    }

    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        delegate.commit();
    }

    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return delegate.getTransaction();
    }

    public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException {
        delegate.resume(tx);
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        delegate.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate.setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        delegate.setTransactionTimeout(seconds);
    }

    public Transaction suspend() throws SystemException {
        return delegate.suspend();
    }
}
