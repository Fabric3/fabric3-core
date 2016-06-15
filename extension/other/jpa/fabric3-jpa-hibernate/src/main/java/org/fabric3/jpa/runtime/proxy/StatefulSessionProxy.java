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

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.fabric3.api.host.Fabric3Exception;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MultiIdentifierLoadAccess;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.query.spi.NativeQueryImplementor;
import org.hibernate.stat.SessionStatistics;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * An Hibernate Session proxy that delegates to a cached instance. This proxy is injected on stateless-scoped components. This proxy is <strong>not</strong>
 * safe to inject on composite-scoped implementations.
 *
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the Session instance associated with the current
 * transaction context from the EntityManagerService. The proxy will cache the Session instance until the transaction completes (or aborts).
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class StatefulSessionProxy implements Session, HibernateProxy {
    private static final long serialVersionUID = 1955430345975268500L;
    private String unitName;
    private EntityManagerService emService;
    private TransactionManager tm;
    private Session session;

    public StatefulSessionProxy(String unitName, EntityManagerService emService, TransactionManager tm) {
        this.unitName = unitName;
        this.emService = emService;
        this.tm = tm;
    }

    @Override
    public void setHibernateFlushMode(FlushMode pFlushMode) {
        initSession();
        session.setHibernateFlushMode(pFlushMode);
    }

    @Override
    public FlushMode getHibernateFlushMode() {
        initSession();
        return session.getHibernateFlushMode();
    }

    @Override
    public boolean contains(String pS, Object pO) {
        initSession();
        return session.contains(pS, pO);
    }

    @Override
    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> pClass) {
        initSession();
        return session.byMultipleIds(pClass);
    }

    @Override
    public MultiIdentifierLoadAccess byMultipleIds(String pS) {
        initSession();
        return session.byMultipleIds(pS);
    }

    @Override
    public <T> Query<T> createQuery(String pS, Class<T> pClass) {
        initSession();
        return session.createQuery(pS, pClass);
    }

    @Override
    public <T> Query<T> createQuery(CriteriaQuery<T> pCriteriaQuery) {
        initSession();
        return session.createQuery(pCriteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate pCriteriaUpdate) {
        initSession();
        return session.createQuery(pCriteriaUpdate);
    }

    @Override
    public Query createQuery(CriteriaDelete pCriteriaDelete) {
        initSession();
        return session.createQuery(pCriteriaDelete);
    }

    @Override
    public <T> Query<T> createNamedQuery(String pS, Class<T> pClass) {
        initSession();
        return session.createNamedQuery(pS, pClass);
    }

    @Override
    public Session getSession() {
        initSession();
        return session;
    }

    @Override
    public void remove(Object pO) {
        initSession();
        session.remove(pO);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO) {
        initSession();
        return session.find(pClass, pO);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO, Map<String, Object> pMap) {
        initSession();
        return session.find(pClass, pO, pMap);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO, LockModeType pLockModeType) {
        initSession();
        return session.find(pClass, pO, pLockModeType);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO, LockModeType pLockModeType, Map<String, Object> pMap) {
        initSession();
        return session.find(pClass, pO, pLockModeType, pMap);
    }

    @Override
    public <T> T getReference(Class<T> pClass, Object pO) {
        initSession();
        return session.getReference(pClass, pO);
    }

    @Override
    public void setFlushMode(FlushModeType pFlushModeType) {
        initSession();
        session.setFlushMode(pFlushModeType);
    }

    @Override
    public void lock(Object pO, LockModeType pLockModeType) {
        initSession();
        session.lock(pO, pLockModeType);
    }

    @Override
    public void lock(Object pO, LockModeType pLockModeType, Map<String, Object> pMap) {
        initSession();
        session.lock(pO, pLockModeType, pMap);
    }

    @Override
    public void refresh(Object pO, Map<String, Object> pMap) {
        initSession();
        session.refresh(pO, pMap);
    }

    @Override
    public void refresh(Object pO, LockModeType pLockModeType) {
        initSession();
        session.refresh(pO, pLockModeType);
    }

    @Override
    public void refresh(Object pO, LockModeType pLockModeType, Map<String, Object> pMap) {
        initSession();
        session.refresh(pO, pLockModeType, pMap);
    }

    @Override
    public void detach(Object pO) {
        initSession();
        session.detach(pO);
    }

    @Override
    public LockModeType getLockMode(Object pO) {
        initSession();
        return session.getLockMode(pO);
    }

    @Override
    public void setProperty(String pS, Object pO) {
        initSession();
        session.setProperty(pS, pO);
    }

    @Override
    public Map<String, Object> getProperties() {
        initSession();
        return session.getProperties();
    }

    @Override
    public NativeQueryImplementor createNativeQuery(String pS, Class pClass) {
        initSession();
        return (NativeQueryImplementor) session.createNativeQuery(pS, pClass);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String pS) {
        initSession();
        return session.createNamedStoredProcedureQuery(pS);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String pS) {
        initSession();
        return session.createStoredProcedureQuery(pS);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String pS, Class... pClasses) {
        initSession();
        return session.createStoredProcedureQuery(pS, pClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String pS, String... pStrings) {
        initSession();
        return session.createStoredProcedureQuery(pS, pStrings);
    }

    @Override
    public void joinTransaction() {
        initSession();
        session.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        initSession();
        return session.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> pClass) {
        initSession();
        return session.unwrap(pClass);
    }

    @Override
    public Object getDelegate() {
        initSession();
        return session.getDelegate();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        initSession();
        return session.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        initSession();
        return session.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        initSession();
        return session.getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> pClass) {
        initSession();
        return session.createEntityGraph(pClass);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String pS) {
        initSession();
        return session.createEntityGraph(pS);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String pS) {
        initSession();
        return session.getEntityGraph(pS);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> pClass) {
        initSession();
        return session.getEntityGraphs(pClass);
    }

    @Override
    public Integer getJdbcBatchSize() {
        initSession();
        return session.getJdbcBatchSize();
    }

    @Override
    public void setJdbcBatchSize(int pI) {
        initSession();
        session.setJdbcBatchSize(pI);
    }

    @Override
    public Query createNamedQuery(String pS) {
        initSession();
        return session.createNamedQuery(pS);
    }

    @Override
    public NativeQuery createNativeQuery(String pS) {
        initSession();
        return session.createNativeQuery(pS);
    }

    @Override
    public NativeQuery createNativeQuery(String pS, String pS1) {
        initSession();
        return session.createNativeQuery(pS, pS1);
    }

    @Override
    public NativeQuery getNamedNativeQuery(String pS) {
        initSession();
        return session.getNamedNativeQuery(pS);
    }

    public void persist(Object entity) {
        initSession();
        session.persist(entity);
    }

    public SharedSessionBuilder sessionWithOptions() {
        initSession();
        return session.sessionWithOptions();
    }

    public void flush() {
        initSession();
        session.flush();
    }

    public void setFlushMode(FlushMode flushMode) {
        initSession();
        session.setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        initSession();
        return session.getFlushMode();
    }

    public void setCacheMode(CacheMode cacheMode) {
        initSession();
        session.setCacheMode(cacheMode);
    }

    public CacheMode getCacheMode() {
        initSession();
        return session.getCacheMode();
    }

    public SessionFactory getSessionFactory() {
        initSession();
        return session.getSessionFactory();
    }

    public void close() throws HibernateException {
        initSession();
        session.close();
    }

    public void cancelQuery() throws HibernateException {
        initSession();
        session.cancelQuery();
    }

    public boolean isOpen() {
        initSession();
        return session.isOpen();
    }

    public boolean isConnected() {
        initSession();
        return session.isConnected();
    }

    public boolean isDirty() throws HibernateException {
        initSession();
        return session.isDirty();
    }

    public boolean isDefaultReadOnly() {
        initSession();
        return session.isDefaultReadOnly();
    }

    public void setDefaultReadOnly(boolean read) {
        initSession();
        session.setDefaultReadOnly(read);
    }

    public Serializable getIdentifier(Object object) throws HibernateException {
        initSession();
        return session.getIdentifier(object);
    }

    public boolean contains(Object object) {
        initSession();
        return session.contains(object);
    }

    public void evict(Object object) throws HibernateException {
        initSession();
        session.evict(object);
    }

    public Object load(Class theClass, Serializable id, LockMode lockMode) throws HibernateException {
        initSession();
        return session.load(theClass, id, lockMode);
    }

    public Object load(Class aClass, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        initSession();
        return session.load(aClass, serializable, lockOptions);
    }

    public Object load(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
        initSession();
        return session.load(entityName, id, lockMode);
    }

    public Object load(String s, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        initSession();
        return session.load(s, serializable, lockOptions);
    }

    public Object load(Class theClass, Serializable id) throws HibernateException {
        initSession();
        return session.load(theClass, id);
    }

    public Object load(String entityName, Serializable id) throws HibernateException {
        initSession();
        return session.load(entityName, id);
    }

    public void load(Object object, Serializable id) throws HibernateException {
        initSession();
        session.load(object, id);
    }

    public void replicate(Object object, ReplicationMode replicationMode) throws HibernateException {
        initSession();
        session.replicate(object, replicationMode);
    }

    public void replicate(String entityName, Object object, ReplicationMode replicationMode) throws HibernateException {
        initSession();
        session.replicate(entityName, object, replicationMode);
    }

    public Serializable save(Object object) throws HibernateException {
        initSession();
        return session.save(object);
    }

    public Serializable save(String entityName, Object object) throws HibernateException {
        initSession();
        return session.save(entityName, object);
    }

    public void saveOrUpdate(Object object) throws HibernateException {
        initSession();
        session.saveOrUpdate(object);
    }

    public void saveOrUpdate(String entityName, Object object) throws HibernateException {
        initSession();
        session.saveOrUpdate(entityName, object);
    }

    public void update(Object object) throws HibernateException {
        initSession();
        session.update(object);
    }

    public void update(String entityName, Object object) throws HibernateException {
        initSession();
        session.update(entityName, object);
    }

    public Object merge(Object object) throws HibernateException {
        initSession();
        return session.merge(object);
    }

    public Object merge(String entityName, Object object) throws HibernateException {
        initSession();
        return session.merge(entityName, object);
    }

    public void persist(String entityName, Object object) throws HibernateException {
        initSession();
        session.persist(entityName, object);
    }

    public void delete(Object object) throws HibernateException {
        initSession();
        session.delete(object);
    }

    public void delete(String entityName, Object object) throws HibernateException {
        initSession();
        session.delete(entityName, object);
    }

    public void lock(Object object, LockMode lockMode) throws HibernateException {
        initSession();
        session.lock(object, lockMode);
    }

    public void lock(String entityName, Object object, LockMode lockMode) throws HibernateException {
        initSession();
        session.lock(entityName, object, lockMode);
    }

    public Session.LockRequest buildLockRequest(LockOptions lockOptions) {
        initSession();
        return session.buildLockRequest(lockOptions);
    }

    public void refresh(Object object) throws HibernateException {
        initSession();
        session.refresh(object);
    }

    public void refresh(String s, Object o) {
    }

    public void refresh(Object object, LockMode lockMode) throws HibernateException {
        initSession();
        session.refresh(object, lockMode);
    }

    public void refresh(Object o, LockOptions lockOptions) throws HibernateException {
        initSession();
        session.refresh(o, lockOptions);
    }

    public void refresh(String s, Object o, LockOptions lockOptions) {
    }

    public LockMode getCurrentLockMode(Object object) throws HibernateException {
        initSession();
        return session.getCurrentLockMode(object);
    }

    public String getTenantIdentifier() {
        initSession();
        return session.getTenantIdentifier();
    }

    public org.hibernate.Transaction beginTransaction() throws HibernateException {
        initSession();
        return session.beginTransaction();
    }

    public org.hibernate.Transaction getTransaction() {
        initSession();
        return session.getTransaction();
    }

    public Criteria createCriteria(Class persistentClass) {
        initSession();
        return session.createCriteria(persistentClass);
    }

    public Criteria createCriteria(Class persistentClass, String alias) {
        initSession();
        return session.createCriteria(persistentClass, alias);
    }

    public Criteria createCriteria(String entityName) {
        initSession();
        return session.createCriteria(entityName);
    }

    public Criteria createCriteria(String entityName, String alias) {
        initSession();
        return session.createCriteria(entityName, alias);
    }

    public Query createQuery(String queryString) throws HibernateException {
        initSession();
        return session.createQuery(queryString);
    }

    public ProcedureCall getNamedProcedureCall(String name) {
        initSession();
        return session.getNamedProcedureCall(name);
    }

    public ProcedureCall createStoredProcedureCall(String procedureName) {
        initSession();
        return session.createStoredProcedureCall(procedureName);
    }

    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        initSession();
        return session.createStoredProcedureCall(procedureName, resultClasses);
    }

    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        initSession();
        return session.createStoredProcedureCall(procedureName, resultSetMappings);
    }

    public Query createFilter(Object collection, String queryString) throws HibernateException {
        initSession();
        return session.createFilter(collection, queryString);
    }

    public Query getNamedQuery(String queryName) throws HibernateException {
        initSession();
        return session.getNamedQuery(queryName);
    }

    public void clear() {
        initSession();
        session.clear();
    }

    public Object get(Class clazz, Serializable id) throws HibernateException {
        initSession();
        return session.get(clazz, id);
    }

    public Object get(Class clazz, Serializable id, LockMode lockMode) throws HibernateException {
        initSession();
        return session.get(clazz, id, lockMode);
    }

    public Object get(Class aClass, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        initSession();
        return session.get(aClass, serializable, lockOptions);
    }

    public Object get(String entityName, Serializable id) throws HibernateException {
        initSession();
        return session.get(entityName, id);
    }

    public Object get(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
        initSession();
        return session.get(entityName, id, lockMode);
    }

    public Object get(String s, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        initSession();
        return session.get(s, serializable, lockOptions);
    }

    public String getEntityName(Object object) throws HibernateException {
        initSession();
        return session.getEntityName(object);
    }

    public IdentifierLoadAccess byId(String s) {
        initSession();
        return session.byId(s);
    }

    public IdentifierLoadAccess byId(Class aClass) {
        initSession();
        return session.byId(aClass);
    }

    public NaturalIdLoadAccess byNaturalId(String s) {
        initSession();
        return session.byNaturalId(s);
    }

    public NaturalIdLoadAccess byNaturalId(Class aClass) {
        initSession();
        return session.byNaturalId(aClass);
    }

    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String s) {
        initSession();
        return session.bySimpleNaturalId(s);
    }

    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class aClass) {
        initSession();
        return session.bySimpleNaturalId(aClass);
    }

    public Filter enableFilter(String filterName) {
        initSession();
        return session.enableFilter(filterName);
    }

    public Filter getEnabledFilter(String filterName) {
        initSession();
        return session.getEnabledFilter(filterName);
    }

    public void disableFilter(String filterName) {
        initSession();
        session.disableFilter(filterName);
    }

    public SessionStatistics getStatistics() {
        initSession();
        return session.getStatistics();
    }

    public boolean isReadOnly(Object o) {
        initSession();
        return session.isReadOnly(o);
    }

    public void setReadOnly(Object entity, boolean readOnly) {
        initSession();
        session.setReadOnly(entity, readOnly);
    }

    public void doWork(Work work) throws HibernateException {
        initSession();
        session.doWork(work);
    }

    public <T> T doReturningWork(ReturningWork<T> tReturningWork) throws HibernateException {
        initSession();
        return session.doReturningWork(tReturningWork);
    }

    public Connection disconnect() throws HibernateException {
        initSession();
        return session.disconnect();
    }

    public void reconnect(Connection connection) throws HibernateException {
        initSession();
        session.reconnect(connection);
    }

    public boolean isFetchProfileEnabled(String s) throws UnknownProfileException {
        initSession();
        return session.isFetchProfileEnabled(s);
    }

    public void enableFetchProfile(String s) throws UnknownProfileException {
        initSession();
        session.enableFetchProfile(s);
    }

    public void disableFetchProfile(String s) throws UnknownProfileException {
        initSession();
        session.disableFetchProfile(s);
    }

    public TypeHelper getTypeHelper() {
        initSession();
        return session.getTypeHelper();
    }

    public LobHelper getLobHelper() {
        initSession();
        return session.getLobHelper();
    }

    public void addEventListeners(SessionEventListener... listeners) {
        initSession();
        session.addEventListeners(listeners);
    }

    public void clearEntityManager() {
        session = null;
    }

    /**
     * Initializes the delegated Session. If the persistence context is transaction-scoped, the Session associated with the current transaction will be used.
     */
    private void initSession() {
        if (session != null) {
            return;
        }
        // a transaction-scoped persistence context
        try {
            Transaction trx = tm.getTransaction();
            if (trx == null) {
                throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
            }
            EntityManager em = emService.getEntityManager(unitName, this, trx);
            session = (Session) em.getDelegate();
        } catch (SystemException | Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }

    }

}