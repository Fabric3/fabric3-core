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
 * An Hibernate Session proxy that delegates to a backing instance. This proxy is injected on composite-scoped components where more than one thread may be
 * accessing the proxy at a time.
 *
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the Session instance associated with the current
 * transaction context from the EntityManagerService.
 */
public class MultiThreadedSessionProxy implements Session, HibernateProxy {
    private static final long serialVersionUID = -4143261157740097948L;
    private String unitName;
    private transient EntityManagerService emService;
    private transient TransactionManager tm;

    public MultiThreadedSessionProxy(String unitName, EntityManagerService emService, TransactionManager tm) {
        this.unitName = unitName;
        this.emService = emService;
        this.tm = tm;
    }

    @Override
    public void setHibernateFlushMode(FlushMode pFlushMode) {
        getSessionLocal().setHibernateFlushMode(pFlushMode);
    }

    @Override
    public FlushMode getHibernateFlushMode() {
        return getSessionLocal().getHibernateFlushMode();
    }

    @Override
    public boolean contains(String pS, Object pO) {
        return getSessionLocal().contains(pS, pO);
    }

    @Override
    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> pClass) {
        return getSessionLocal().byMultipleIds(pClass);
    }

    @Override
    public MultiIdentifierLoadAccess byMultipleIds(String pS) {
        return getSessionLocal().byMultipleIds(pS);
    }

    @Override
    public <T> Query<T> createQuery(String pS, Class<T> pClass) {
        return getSessionLocal().createQuery(pS, pClass);
    }

    @Override
    public <T> Query<T> createQuery(CriteriaQuery<T> pCriteriaQuery) {
        return getSessionLocal().createQuery(pCriteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate pCriteriaUpdate) {
        return getSessionLocal().createQuery(pCriteriaUpdate);
    }

    @Override
    public Query createQuery(CriteriaDelete pCriteriaDelete) {
        return getSessionLocal().createQuery(pCriteriaDelete);
    }

    @Override
    public <T> Query<T> createNamedQuery(String pS, Class<T> pClass) {
        return getSessionLocal().createNamedQuery(pS, pClass);
    }

    @Override
    public Session getSession() {
        return getSessionLocal();
    }

    @Override
    public void remove(Object pO) {
        getSessionLocal().remove(pO);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO) {
        return getSessionLocal().find(pClass, pO);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO, Map<String, Object> pMap) {
        return getSessionLocal().find(pClass, pO, pMap);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO, LockModeType pLockModeType) {
        return getSessionLocal().find(pClass, pO, pLockModeType);
    }

    @Override
    public <T> T find(Class<T> pClass, Object pO, LockModeType pLockModeType, Map<String, Object> pMap) {
        return getSessionLocal().find(pClass, pO, pLockModeType, pMap);
    }

    @Override
    public <T> T getReference(Class<T> pClass, Object pO) {
        return getSessionLocal().getReference(pClass, pO);
    }

    @Override
    public void setFlushMode(FlushModeType pFlushModeType) {
        getSessionLocal().setFlushMode(pFlushModeType);
    }

    @Override
    public void lock(Object pO, LockModeType pLockModeType) {
        getSessionLocal().lock(pO, pLockModeType);
    }

    @Override
    public void lock(Object pO, LockModeType pLockModeType, Map<String, Object> pMap) {
        getSessionLocal().lock(pO, pLockModeType, pMap);
    }

    @Override
    public void refresh(Object pO, Map<String, Object> pMap) {
        getSessionLocal().refresh(pO, pMap);
    }

    @Override
    public void refresh(Object pO, LockModeType pLockModeType) {
        getSessionLocal().refresh(pO, pLockModeType);
    }

    @Override
    public void refresh(Object pO, LockModeType pLockModeType, Map<String, Object> pMap) {
        getSessionLocal().refresh(pO, pLockModeType, pMap);
    }

    @Override
    public void detach(Object pO) {
        getSessionLocal().detach(pO);
    }

    @Override
    public LockModeType getLockMode(Object pO) {
        return getSessionLocal().getLockMode(pO);
    }

    @Override
    public void setProperty(String pS, Object pO) {
        getSessionLocal().setProperty(pS, pO);
    }

    @Override
    public Map<String, Object> getProperties() {
        return getSessionLocal().getProperties();
    }

    @Override
    public NativeQueryImplementor createNativeQuery(String pS, Class pClass) {
        return (NativeQueryImplementor) getSessionLocal().createNativeQuery(pS, pClass);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String pS) {
        return getSessionLocal().createNamedStoredProcedureQuery(pS);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String pS) {
        return getSessionLocal().createStoredProcedureQuery(pS);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String pS, Class... pClasses) {
        return getSessionLocal().createStoredProcedureQuery(pS, pClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String pS, String... pStrings) {
        return getSessionLocal().createStoredProcedureQuery(pS, pStrings);
    }

    @Override
    public void joinTransaction() {
        getSessionLocal().joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return getSessionLocal().isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> pClass) {
        return getSessionLocal().unwrap(pClass);
    }

    @Override
    public Object getDelegate() {
        return getSessionLocal().getDelegate();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getSessionLocal().getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getSessionLocal().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return getSessionLocal().getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> pClass) {
        return getSessionLocal().createEntityGraph(pClass);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String pS) {
        return getSessionLocal().createEntityGraph(pS);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String pS) {
        return getSessionLocal().getEntityGraph(pS);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> pClass) {
        return getSessionLocal().getEntityGraphs(pClass);
    }

    @Override
    public Integer getJdbcBatchSize() {
        return getSessionLocal().getJdbcBatchSize();
    }

    @Override
    public void setJdbcBatchSize(int pI) {
        getSessionLocal().setJdbcBatchSize(pI);
    }

    @Override
    public Query createNamedQuery(String pS) {
        return getSessionLocal().createNamedQuery(pS);
    }

    @Override
    public NativeQuery createNativeQuery(String pS) {
        return getSessionLocal().createNativeQuery(pS);
    }

    @Override
    public NativeQuery createNativeQuery(String pS, String pS1) {
        return getSessionLocal().createNativeQuery(pS, pS1);
    }

    @Override
    public NativeQuery getNamedNativeQuery(String pS) {
        return getSessionLocal().getNamedNativeQuery(pS);
    }

    public void persist(Object entity) {
        getSessionLocal().persist(entity);
    }

    public SharedSessionBuilder sessionWithOptions() {
        return getSessionLocal().sessionWithOptions();
    }

    public void flush() {
        getSessionLocal().flush();
    }

    public void setFlushMode(FlushMode flushMode) {
        getSessionLocal().setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        return getSessionLocal().getFlushMode();
    }

    public void setCacheMode(CacheMode cacheMode) {
        getSessionLocal().setCacheMode(cacheMode);
    }

    public CacheMode getCacheMode() {
        return getSessionLocal().getCacheMode();
    }

    public SessionFactory getSessionFactory() {
        return getSessionLocal().getSessionFactory();
    }

    public void close() throws HibernateException {
        getSessionLocal().close();
    }

    public void cancelQuery() throws HibernateException {
        getSessionLocal().cancelQuery();
    }

    public boolean isOpen() {
        return getSessionLocal().isOpen();
    }

    public boolean isConnected() {
        return getSessionLocal().isConnected();
    }

    public boolean isDirty() throws HibernateException {
        return getSessionLocal().isDirty();
    }

    public boolean isDefaultReadOnly() {
        return getSessionLocal().isDefaultReadOnly();
    }

    public void setDefaultReadOnly(boolean b) {
        getSessionLocal().setDefaultReadOnly(b);
    }

    public Serializable getIdentifier(Object object) throws HibernateException {
        return getSessionLocal().getIdentifier(object);
    }

    public boolean contains(Object object) {
        return getSessionLocal().contains(object);
    }

    public void evict(Object object) throws HibernateException {
        getSessionLocal().evict(object);
    }

    public Object load(Class theClass, Serializable id, LockMode lockMode) throws HibernateException {
        return theClass.cast(getSessionLocal().load(theClass, id, lockMode));
    }

    public Object load(Class theClass, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSessionLocal().load(theClass, serializable, lockOptions);
    }

    public Object load(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
        return getSessionLocal().load(entityName, id, lockMode);
    }

    public Object load(String s, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSessionLocal().load(s, serializable, lockOptions);
    }

    public Object load(Class theClass, Serializable id) throws HibernateException {
        return getSessionLocal().load(theClass, id);
    }

    public Object load(String entityName, Serializable id) throws HibernateException {
        return getSessionLocal().load(entityName, id);
    }

    public void load(Object object, Serializable id) throws HibernateException {
        getSessionLocal().load(object, id);
    }

    public void replicate(Object object, ReplicationMode replicationMode) throws HibernateException {
        getSessionLocal().replicate(object, replicationMode);
    }

    public void replicate(String entityName, Object object, ReplicationMode replicationMode) throws HibernateException {
        getSessionLocal().replicate(entityName, object, replicationMode);
    }

    public Serializable save(Object object) throws HibernateException {
        return getSessionLocal().save(object);
    }

    public Serializable save(String entityName, Object object) throws HibernateException {
        return getSessionLocal().save(entityName, object);
    }

    public void saveOrUpdate(Object object) throws HibernateException {
        getSessionLocal().saveOrUpdate(object);
    }

    public void saveOrUpdate(String entityName, Object object) throws HibernateException {
        getSessionLocal().saveOrUpdate(entityName, object);
    }

    public void update(Object object) throws HibernateException {
        getSessionLocal().update(object);
    }

    public void update(String entityName, Object object) throws HibernateException {
        getSessionLocal().update(entityName, object);
    }

    public Object merge(Object object) throws HibernateException {
        return getSessionLocal().merge(object);
    }

    public Object merge(String entityName, Object object) throws HibernateException {
        return getSessionLocal().merge(entityName, object);
    }

    public void persist(String entityName, Object object) throws HibernateException {
        getSessionLocal().persist(entityName, object);
    }

    public void delete(Object object) throws HibernateException {
        getSessionLocal().delete(object);
    }

    public void delete(String entityName, Object object) throws HibernateException {
        getSessionLocal().delete(entityName, object);
    }

    public void lock(Object object, LockMode lockMode) throws HibernateException {
        getSessionLocal().lock(object, lockMode);
    }

    public void lock(String entityName, Object object, LockMode lockMode) throws HibernateException {
        getSessionLocal().lock(entityName, object, lockMode);
    }

    public Session.LockRequest buildLockRequest(LockOptions lockOptions) {
        return getSessionLocal().buildLockRequest(lockOptions);
    }

    public void refresh(Object object) throws HibernateException {
        getSessionLocal().refresh(object);
    }

    public void refresh(String s, Object o) {
    }

    public void refresh(Object object, LockMode lockMode) throws HibernateException {
        getSessionLocal().refresh(object, lockMode);
    }

    public void refresh(Object o, LockOptions lockOptions) throws HibernateException {
        getSessionLocal().refresh(o, lockOptions);
    }

    public void refresh(String s, Object o, LockOptions lockOptions) {
    }

    public LockMode getCurrentLockMode(Object object) throws HibernateException {
        return getSessionLocal().getCurrentLockMode(object);
    }

    public String getTenantIdentifier() {
        return getSessionLocal().getTenantIdentifier();
    }

    public org.hibernate.Transaction beginTransaction() throws HibernateException {
        return getSessionLocal().beginTransaction();
    }

    public org.hibernate.Transaction getTransaction() {
        return getSessionLocal().getTransaction();
    }

    public Criteria createCriteria(Class persistentClass) {
        return getSessionLocal().createCriteria(persistentClass);
    }

    public Criteria createCriteria(Class persistentClass, String alias) {
        return getSessionLocal().createCriteria(persistentClass, alias);
    }

    public Criteria createCriteria(String entityName) {
        return getSessionLocal().createCriteria(entityName);
    }

    public Criteria createCriteria(String entityName, String alias) {
        return getSessionLocal().createCriteria(entityName, alias);
    }

    public Query createQuery(String queryString) throws HibernateException {
        return getSessionLocal().createQuery(queryString);
    }

    public ProcedureCall getNamedProcedureCall(String name) {
        return getSessionLocal().getNamedProcedureCall(name);
    }

    
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return getSessionLocal().createStoredProcedureCall(procedureName);
    }

    
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return getSessionLocal().createStoredProcedureCall(procedureName, resultClasses);
    }

    
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return getSessionLocal().createStoredProcedureCall(procedureName, resultSetMappings);
    }

    public Query createFilter(Object collection, String queryString) throws HibernateException {
        return getSessionLocal().createFilter(collection, queryString);
    }

    public Query getNamedQuery(String queryName) throws HibernateException {
        return getSessionLocal().getNamedQuery(queryName);
    }

    public void clear() {
        getSessionLocal().clear();
    }

    public Object get(Class clazz, Serializable id) throws HibernateException {
        return getSessionLocal().get(clazz, id);
    }

    public Object get(Class clazz, Serializable id, LockMode lockMode) throws HibernateException {
        return getSessionLocal().get(clazz, id, lockMode);
    }

    public Object get(Class aClass, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSessionLocal().get(aClass, serializable, lockOptions);
    }

    public Object get(String entityName, Serializable id) throws HibernateException {
        return getSessionLocal().get(entityName, id);
    }

    public Object get(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
        return getSessionLocal().get(entityName, id, lockMode);
    }

    public Object get(String s, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSessionLocal().get(s, serializable, lockOptions);
    }

    public String getEntityName(Object object) throws HibernateException {
        return getSessionLocal().getEntityName(object);
    }

    public IdentifierLoadAccess byId(String s) {
        return getSessionLocal().byId(s);
    }

    public IdentifierLoadAccess byId(Class aClass) {
        return (getSessionLocal().byId(aClass));
    }

    public NaturalIdLoadAccess byNaturalId(String s) {
        return getSessionLocal().byNaturalId(s);
    }

    public NaturalIdLoadAccess byNaturalId(Class aClass) {
        return getSessionLocal().byNaturalId(aClass);
    }

    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String s) {
        return getSessionLocal().bySimpleNaturalId(s);
    }

    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class aClass) {
        return getSessionLocal().bySimpleNaturalId(aClass);
    }

    public Filter enableFilter(String filterName) {
        return getSessionLocal().enableFilter(filterName);
    }

    public Filter getEnabledFilter(String filterName) {
        return getSessionLocal().getEnabledFilter(filterName);
    }

    public void disableFilter(String filterName) {
        getSessionLocal().disableFilter(filterName);
    }

    public SessionStatistics getStatistics() {
        return getSessionLocal().getStatistics();
    }

    public boolean isReadOnly(Object o) {
        return getSessionLocal().isReadOnly(o);
    }

    public void setReadOnly(Object entity, boolean readOnly) {
        getSessionLocal().setReadOnly(entity, readOnly);
    }

    public void doWork(Work work) throws HibernateException {
        getSessionLocal().doWork(work);
    }

    public <T> T doReturningWork(ReturningWork<T> tReturningWork) throws HibernateException {
        return getSessionLocal().doReturningWork(tReturningWork);
    }

    public Connection disconnect() throws HibernateException {
        return getSessionLocal().disconnect();
    }

    public void reconnect(Connection connection) throws HibernateException {
        getSessionLocal().reconnect(connection);
    }

    public boolean isFetchProfileEnabled(String s) throws UnknownProfileException {
        return getSessionLocal().isFetchProfileEnabled(s);
    }

    public void enableFetchProfile(String s) throws UnknownProfileException {
        getSessionLocal().enableFetchProfile(s);
    }

    public void disableFetchProfile(String s) throws UnknownProfileException {
        getSessionLocal().disableFetchProfile(s);
    }

    public TypeHelper getTypeHelper() {
        return getSessionLocal().getTypeHelper();
    }

    public LobHelper getLobHelper() {
        return getSessionLocal().getLobHelper();
    }


    public void addEventListeners(SessionEventListener... listeners) {
        getSessionLocal().addEventListeners(listeners);
    }

    /**
     * Returns the delegated Session. If the persistence context is transaction-scoped, the Session associated with the current transaction will be used.
     *
     * @return the Session
     */
    private Session getSessionLocal() {
        // a transaction-scoped persistence context
        try {
            Transaction trx = tm.getTransaction();
            if (trx == null) {
                throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
            }
            EntityManager em = emService.getEntityManager(unitName, this, trx);
            return (Session) em.getDelegate();
        } catch (SystemException | Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void clearEntityManager() {

    }
}