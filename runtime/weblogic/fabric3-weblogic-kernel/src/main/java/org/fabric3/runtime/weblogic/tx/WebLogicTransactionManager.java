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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

/**
 * Implementation that delegates to the WebLogic TransactionManager.
 *
 * @version $Rev$ $Date$
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
