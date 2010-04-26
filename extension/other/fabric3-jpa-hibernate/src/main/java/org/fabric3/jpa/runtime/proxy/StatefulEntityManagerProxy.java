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
package org.fabric3.jpa.runtime.proxy;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * An EntityManager proxy that delegates to a cached instance. This proxy is injected on stateless and conversation-scoped components. This proxy is
 * <strong>not</strong> safe to inject on composite-scoped implementations.
 * <p/>
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the EntityManager instance associated with
 * the current transaction context from the EntityManagerService. If the persistence context is extended (as defined by JPA), the proxy will attempt
 * to retrieve the EntityManager instance associated with the current conversation. The proxy will cache the EntityManager instance until the
 * transaction completes (or aborts) or the conversation ends.
 *
 * @version $Rev$ $Date$
 */
public class StatefulEntityManagerProxy implements HibernateProxy, EntityManager {
    private String unitName;
    private boolean extended;
    private EntityManager em;
    private EntityManagerService emService;
    private TransactionManager tm;

    public StatefulEntityManagerProxy(String unitName, boolean extended, EntityManagerService emService, TransactionManager tm) {
        this.unitName = unitName;
        this.extended = extended;
        this.emService = emService;
        this.tm = tm;
    }

    public void persist(Object entity) {
        initEntityManager();
        em.persist(entity);
    }

    public <T> T merge(T entity) {
        initEntityManager();
        return em.merge(entity);
    }

    public void remove(Object entity) {
        initEntityManager();
        em.remove(entity);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        initEntityManager();
        return em.find(entityClass, primaryKey);
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        initEntityManager();
        return em.getReference(entityClass, primaryKey);
    }

    public void flush() {
        initEntityManager();
        em.flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        initEntityManager();
        em.setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        initEntityManager();
        return em.getFlushMode();
    }

    public void lock(Object entity, LockModeType lockMode) {
        initEntityManager();
        em.lock(entity, lockMode);
    }

    public void refresh(Object entity) {
        initEntityManager();
        em.remove(entity);
    }

    public void clear() {
        initEntityManager();
        em.clear();
    }

    public boolean contains(Object entity) {
        initEntityManager();
        return em.contains(entity);
    }

    public Query createQuery(String qlString) {
        initEntityManager();
        return em.createQuery(qlString);
    }

    public Query createNamedQuery(String name) {
        initEntityManager();
        return em.createNamedQuery(name);
    }

    public Query createNativeQuery(String sqlString) {
        initEntityManager();
        return em.createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        initEntityManager();
        return em.createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        initEntityManager();
        return em.createNativeQuery(sqlString, resultSetMapping);
    }

    public void joinTransaction() {
        initEntityManager();
        em.joinTransaction();
    }

    public Object getDelegate() {
        initEntityManager();
        return em.getDelegate();
    }

    public void close() {
        initEntityManager();
        em.close();
    }

    public boolean isOpen() {
        initEntityManager();
        return em.isOpen();
    }

    public EntityTransaction getTransaction() {
        initEntityManager();
        return em.getTransaction();
    }

    public void clearEntityManager() {
        em = null;
    }

    /**
     * Initalizes the delegated EntityManager. If the persistence context is transaction-scoped, the EntityManager associated with the current
     * transaction will be used. Otherwise, if the persistence context is extended, the EntityManager associated with the current conversation will be
     * used.
     */
    private void initEntityManager() {
        if (em != null) {
            return;
        }
        if (extended) {
            // an extended persistence context, associate it with the current conversation
            WorkContext context = WorkContextTunnel.getThreadWorkContext();
            F3Conversation conversation = context.peekCallFrame().getConversation();
            if (conversation == null) {
                throw new IllegalStateException("No conversational context associated with the current component");
            }
            try {
                em = emService.getEntityManager(unitName, this, conversation);
            } catch (EntityManagerCreationException e) {
                throw new ServiceRuntimeException(e);
            }
        } else {
            // a transaction-scoped persitence context
            try {
                Transaction trx = tm.getTransaction();
                if (trx == null) {
                    throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
                }
                em = emService.getEntityManager(unitName, this, trx);
            } catch (SystemException e) {
                throw new ServiceRuntimeException(e);
            } catch (EntityManagerCreationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

}
