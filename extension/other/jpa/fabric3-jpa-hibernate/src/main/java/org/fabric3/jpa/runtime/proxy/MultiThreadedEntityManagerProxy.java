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

import org.fabric3.api.host.Fabric3Exception;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * An EntityManager proxy that delegates to a backing instance. This proxy is injected on composite-scoped components where more than one thread may be
 * accessing the proxy at a time.
 * <p/>
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the EntityManager instance associated with the
 * current transaction context from the EntityManagerService.
 */
public class MultiThreadedEntityManagerProxy implements HibernateProxy, EntityManager {
    private String unitName;
    private EntityManagerService emService;
    private TransactionManager tm;

    public MultiThreadedEntityManagerProxy(String unitName, EntityManagerService emService, TransactionManager tm) {
        this.unitName = unitName;
        this.emService = emService;
        this.tm = tm;
    }

    public void persist(Object entity) {
        getEntityManager().persist(entity);
    }

    public <T> T merge(T entity) {
        return getEntityManager().merge(entity);
    }

    public void remove(Object entity) {
        getEntityManager().remove(entity);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().find(entityClass, primaryKey);
    }

    public <T> T find(Class<T> tClass, Object o, Map<String, Object> stringObjectMap) {
        return getEntityManager().find(tClass, o, stringObjectMap);
    }

    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType) {
        return getEntityManager().find(tClass, o, lockModeType);
    }

    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        return getEntityManager().find(tClass, o, lockModeType, stringObjectMap);
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getEntityManager().getReference(entityClass, primaryKey);
    }

    public void flush() {
        getEntityManager().flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        getEntityManager().setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    public void lock(Object entity, LockModeType lockMode) {
        getEntityManager().lock(entity, lockMode);
    }

    public void lock(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        getEntityManager().lock(o, lockModeType, stringObjectMap);
    }

    public void refresh(Object entity) {
        getEntityManager().remove(entity);
    }

    public void refresh(Object o, Map<String, Object> stringObjectMap) {
        getEntityManager().refresh(o, stringObjectMap);
    }

    public void refresh(Object o, LockModeType lockModeType) {
        getEntityManager().refresh(o, lockModeType);
    }

    public void refresh(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        getEntityManager().refresh(o, lockModeType, stringObjectMap);
    }

    public void clear() {
        getEntityManager().clear();
    }

    public void detach(Object o) {
        getEntityManager().detach(o);
    }

    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    public LockModeType getLockMode(Object o) {
        return getEntityManager().getLockMode(o);
    }

    public void setProperty(String s, Object o) {
        getEntityManager().setProperty(s, o);
    }

    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    public Query createQuery(String qlString) {
        return getEntityManager().createQuery(qlString);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> tCriteriaQuery) {
        return getEntityManager().createQuery(tCriteriaQuery);
    }

    public Query createQuery(CriteriaUpdate updateQuery) {
        return getEntityManager().createQuery(updateQuery);
    }

    public Query createQuery(CriteriaDelete deleteQuery) {
        return getEntityManager().createQuery(deleteQuery);
    }

    public <T> TypedQuery<T> createQuery(String s, Class<T> tClass) {
        return getEntityManager().createQuery(s, tClass);
    }

    public Query createNamedQuery(String name) {
        return getEntityManager().createNamedQuery(name);
    }

    public <T> TypedQuery<T> createNamedQuery(String s, Class<T> tClass) {
        return getEntityManager().createNamedQuery(s, tClass);
    }

    public Query createNativeQuery(String sqlString) {
        return getEntityManager().createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        return getEntityManager().createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return getEntityManager().createNamedStoredProcedureQuery(name);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return getEntityManager().createStoredProcedureQuery(procedureName);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return getEntityManager().createStoredProcedureQuery(procedureName, resultClasses);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return getEntityManager().createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    public void joinTransaction() {
        getEntityManager().joinTransaction();
    }

    public boolean isJoinedToTransaction() {
        return getEntityManager().isJoinedToTransaction();
    }

    public <T> T unwrap(Class<T> tClass) {
        return getEntityManager().unwrap(tClass);
    }

    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    public void close() {
        getEntityManager().close();
    }

    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return getEntityManager().createEntityGraph(rootType);
    }

    public EntityGraph<?> createEntityGraph(String graphName) {
        return getEntityManager().createEntityGraph(graphName);
    }

    public EntityGraph<?> getEntityGraph(String graphName) {
        return getEntityManager().getEntityGraph(graphName);
    }

    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return getEntityManager().getEntityGraphs(entityClass);
    }

    public void clearEntityManager() {
        // no-op
    }

    /**
     * Returns the delegated EntityManager. If the persistence context is transaction-scoped, the EntityManager associated with the current transaction will be
     * used.
     *
     * @return the EntityManager
     */
    private EntityManager getEntityManager() {
        // a transaction-scoped persistence context
        try {
            Transaction trx = tm.getTransaction();
            if (trx == null) {
                throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
            }
            return emService.getEntityManager(unitName, this, trx);
        } catch (SystemException | Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

}