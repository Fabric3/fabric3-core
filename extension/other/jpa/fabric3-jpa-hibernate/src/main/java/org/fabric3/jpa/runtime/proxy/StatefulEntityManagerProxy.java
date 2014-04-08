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
package org.fabric3.jpa.runtime.proxy;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.ServiceRuntimeException;

/**
 * An EntityManager proxy that delegates to a cached instance. This proxy is injected on stateless-scoped components. This proxy is <strong>not</strong> safe to
 * inject on composite-scoped implementations.
 * <p/>
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the EntityManager instance associated with the
 * current transaction context from the EntityManagerService. The proxy will cache the EntityManager instance until the transaction completes.
 */
public class StatefulEntityManagerProxy implements HibernateProxy, EntityManager {
    private String unitName;
    private EntityManager em;
    private EntityManagerService emService;
    private TransactionManager tm;

    public StatefulEntityManagerProxy(String unitName, EntityManagerService emService, TransactionManager tm) {
        this.unitName = unitName;
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

    public <T> T find(Class<T> tClass, Object o, Map<String, Object> stringObjectMap) {
        initEntityManager();
        return em.find(tClass, o, stringObjectMap);
    }

    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType) {
        initEntityManager();
        return em.find(tClass, o, lockModeType);
    }

    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        initEntityManager();
        return em.find(tClass, o, lockModeType, stringObjectMap);
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

    public void lock(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        initEntityManager();
        em.lock(o, lockModeType, stringObjectMap);
    }

    public void refresh(Object entity) {
        initEntityManager();
        em.remove(entity);
    }

    public void refresh(Object o, Map<String, Object> stringObjectMap) {
        initEntityManager();
        em.refresh(o, stringObjectMap);
    }

    public void refresh(Object o, LockModeType lockModeType) {
        initEntityManager();
        em.refresh(o, lockModeType);
    }

    public void refresh(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        initEntityManager();
        em.refresh(o, lockModeType, stringObjectMap);
    }

    public void clear() {
        initEntityManager();
        em.clear();
    }

    public void detach(Object o) {
        initEntityManager();
        em.detach(o);
    }

    public boolean contains(Object entity) {
        initEntityManager();
        return em.contains(entity);
    }

    public LockModeType getLockMode(Object o) {
        initEntityManager();
        return em.getLockMode(o);
    }

    public void setProperty(String s, Object o) {
        initEntityManager();
        em.setProperty(s, o);
    }

    public Map<String, Object> getProperties() {
        initEntityManager();
        return em.getProperties();
    }

    public Query createQuery(String qlString) {
        initEntityManager();
        return em.createQuery(qlString);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> tCriteriaQuery) {
        initEntityManager();
        return em.createQuery(tCriteriaQuery);
    }

    
    public Query createQuery(CriteriaUpdate updateQuery) {
        initEntityManager();
        return em.createQuery(updateQuery);
    }

    
    public Query createQuery(CriteriaDelete deleteQuery) {
        initEntityManager();
        return em.createQuery(deleteQuery);
    }

    public <T> TypedQuery<T> createQuery(String s, Class<T> tClass) {
        initEntityManager();
        return em.createQuery(s, tClass);
    }

    public Query createNamedQuery(String name) {
        initEntityManager();
        return em.createNamedQuery(name);
    }

    public <T> TypedQuery<T> createNamedQuery(String s, Class<T> tClass) {
        initEntityManager();
        return em.createNamedQuery(s, tClass);
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

    
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        initEntityManager();
        return em.createNamedStoredProcedureQuery(name);
    }

    
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        initEntityManager();
        return em.createStoredProcedureQuery(procedureName);
    }

    
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        initEntityManager();
        return em.createStoredProcedureQuery(procedureName, resultClasses);
    }

    
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        initEntityManager();
        return em.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    public void joinTransaction() {
        initEntityManager();
        em.joinTransaction();
    }

    
    public boolean isJoinedToTransaction() {
        initEntityManager();
        return em.isJoinedToTransaction();
    }

    public <T> T unwrap(Class<T> tClass) {
        initEntityManager();
        return em.unwrap(tClass);
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

    public EntityManagerFactory getEntityManagerFactory() {
        initEntityManager();
        return em.getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        initEntityManager();
        return em.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        initEntityManager();
        return em.getMetamodel();
    }

    
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        initEntityManager();
        return em.createEntityGraph(rootType);
    }

    
    public EntityGraph<?> createEntityGraph(String graphName) {
        initEntityManager();
        return em.createEntityGraph(graphName);
    }

    
    public EntityGraph<?> getEntityGraph(String graphName) {
        initEntityManager();
        return em.getEntityGraph(graphName);
    }

    
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        initEntityManager();
        return em.getEntityGraphs(entityClass);
    }

    public void clearEntityManager() {
        em = null;
    }

    /**
     * Initializes the delegated EntityManager. If the persistence context is transaction-scoped, the EntityManager associated with the current transaction will
     * be used. Otherwise, if the persistence context is extended, the EntityManager associated with the current conversation will be used.
     */
    private void initEntityManager() {
        if (em != null) {
            return;
        }
        // a transaction-scoped persistence context
        try {
            Transaction trx = tm.getTransaction();
            if (trx == null) {
                throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
            }
            em = emService.getEntityManager(unitName, this, trx);
        } catch (SystemException | EntityManagerCreationException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
