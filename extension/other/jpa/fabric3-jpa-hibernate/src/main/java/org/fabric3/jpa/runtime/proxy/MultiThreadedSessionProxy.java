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

import javax.persistence.EntityManager;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.sql.Connection;

import org.fabric3.spi.container.ContainerException;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
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
import org.hibernate.stat.SessionStatistics;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * An Hibernate Session proxy that delegates to a backing instance. This proxy is injected on composite-scoped components where more than one thread may be
 * accessing the proxy at a time.
 * <p/>
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

    public void persist(Object entity) {
        getSession().persist(entity);
    }

    public SharedSessionBuilder sessionWithOptions() {
        return getSession().sessionWithOptions();
    }

    public void flush() {
        getSession().flush();
    }

    public void setFlushMode(FlushMode flushMode) {
        getSession().setFlushMode(flushMode);
    }

    public FlushMode getFlushMode() {
        return getSession().getFlushMode();
    }

    public void setCacheMode(CacheMode cacheMode) {
        getSession().setCacheMode(cacheMode);
    }

    public CacheMode getCacheMode() {
        return getSession().getCacheMode();
    }

    public SessionFactory getSessionFactory() {
        return getSession().getSessionFactory();
    }

    public Connection close() throws HibernateException {
        return getSession().close();
    }

    public void cancelQuery() throws HibernateException {
        getSession().cancelQuery();
    }

    public boolean isOpen() {
        return getSession().isOpen();
    }

    public boolean isConnected() {
        return getSession().isConnected();
    }

    public boolean isDirty() throws HibernateException {
        return getSession().isDirty();
    }

    public boolean isDefaultReadOnly() {
        return getSession().isDefaultReadOnly();
    }

    public void setDefaultReadOnly(boolean b) {
        getSession().setDefaultReadOnly(b);
    }

    public Serializable getIdentifier(Object object) throws HibernateException {
        return getSession().getIdentifier(object);
    }

    public boolean contains(Object object) {
        return getSession().contains(object);
    }

    public void evict(Object object) throws HibernateException {
        getSession().evict(object);
    }

    public Object load(Class theClass, Serializable id, LockMode lockMode) throws HibernateException {
        return getSession().load(theClass, id, lockMode);
    }

    public Object load(Class aClass, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSession().load(aClass, serializable, lockOptions);
    }

    public Object load(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
        return getSession().load(entityName, id, lockMode);
    }

    public Object load(String s, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSession().load(s, serializable, lockOptions);
    }

    public Object load(Class theClass, Serializable id) throws HibernateException {
        return getSession().load(theClass, id);
    }

    public Object load(String entityName, Serializable id) throws HibernateException {
        return getSession().load(entityName, id);
    }

    public void load(Object object, Serializable id) throws HibernateException {
        getSession().load(object, id);
    }

    public void replicate(Object object, ReplicationMode replicationMode) throws HibernateException {
        getSession().replicate(object, replicationMode);
    }

    public void replicate(String entityName, Object object, ReplicationMode replicationMode) throws HibernateException {
        getSession().replicate(entityName, object, replicationMode);
    }

    public Serializable save(Object object) throws HibernateException {
        return getSession().save(object);
    }

    public Serializable save(String entityName, Object object) throws HibernateException {
        return getSession().save(entityName, object);
    }

    public void saveOrUpdate(Object object) throws HibernateException {
        getSession().saveOrUpdate(object);
    }

    public void saveOrUpdate(String entityName, Object object) throws HibernateException {
        getSession().saveOrUpdate(entityName, object);
    }

    public void update(Object object) throws HibernateException {
        getSession().update(object);
    }

    public void update(String entityName, Object object) throws HibernateException {
        getSession().update(entityName, object);
    }

    public Object merge(Object object) throws HibernateException {
        return getSession().merge(object);
    }

    public Object merge(String entityName, Object object) throws HibernateException {
        return getSession().merge(entityName, object);
    }

    public void persist(String entityName, Object object) throws HibernateException {
        getSession().persist(entityName, object);
    }

    public void delete(Object object) throws HibernateException {
        getSession().delete(object);
    }

    public void delete(String entityName, Object object) throws HibernateException {
        getSession().delete(entityName, object);
    }

    public void lock(Object object, LockMode lockMode) throws HibernateException {
        getSession().lock(object, lockMode);
    }

    public void lock(String entityName, Object object, LockMode lockMode) throws HibernateException {
        getSession().lock(entityName, object, lockMode);
    }

    public Session.LockRequest buildLockRequest(LockOptions lockOptions) {
        return getSession().buildLockRequest(lockOptions);
    }

    public void refresh(Object object) throws HibernateException {
        getSession().refresh(object);
    }

    public void refresh(String s, Object o) {
    }

    public void refresh(Object object, LockMode lockMode) throws HibernateException {
        getSession().refresh(object, lockMode);
    }

    public void refresh(Object o, LockOptions lockOptions) throws HibernateException {
        getSession().refresh(o, lockOptions);
    }

    public void refresh(String s, Object o, LockOptions lockOptions) {
    }

    public LockMode getCurrentLockMode(Object object) throws HibernateException {
        return getSession().getCurrentLockMode(object);
    }

    public String getTenantIdentifier() {
        return getSession().getTenantIdentifier();
    }

    public org.hibernate.Transaction beginTransaction() throws HibernateException {
        return getSession().beginTransaction();
    }

    public org.hibernate.Transaction getTransaction() {
        return getSession().getTransaction();
    }

    public Criteria createCriteria(Class persistentClass) {
        return getSession().createCriteria(persistentClass);
    }

    public Criteria createCriteria(Class persistentClass, String alias) {
        return getSession().createCriteria(persistentClass, alias);
    }

    public Criteria createCriteria(String entityName) {
        return getSession().createCriteria(entityName);
    }

    public Criteria createCriteria(String entityName, String alias) {
        return getSession().createCriteria(entityName, alias);
    }

    public org.hibernate.Query createQuery(String queryString) throws HibernateException {
        return getSession().createQuery(queryString);
    }

    public SQLQuery createSQLQuery(String queryString) throws HibernateException {
        return getSession().createSQLQuery(queryString);
    }

    
    public ProcedureCall getNamedProcedureCall(String name) {
        return getSession().getNamedProcedureCall(name);
    }

    
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return getSession().createStoredProcedureCall(procedureName);
    }

    
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return getSession().createStoredProcedureCall(procedureName, resultClasses);
    }

    
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return getSession().createStoredProcedureCall(procedureName, resultSetMappings);
    }

    public org.hibernate.Query createFilter(Object collection, String queryString) throws HibernateException {
        return getSession().createFilter(collection, queryString);
    }

    public org.hibernate.Query getNamedQuery(String queryName) throws HibernateException {
        return getSession().getNamedQuery(queryName);
    }

    public void clear() {
        getSession().clear();
    }

    public Object get(Class clazz, Serializable id) throws HibernateException {
        return getSession().get(clazz, id);
    }

    public Object get(Class clazz, Serializable id, LockMode lockMode) throws HibernateException {
        return getSession().get(clazz, id, lockMode);
    }

    public Object get(Class aClass, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSession().get(aClass, serializable, lockOptions);
    }

    public Object get(String entityName, Serializable id) throws HibernateException {
        return getSession().get(entityName, id);
    }

    public Object get(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
        return getSession().get(entityName, id, lockMode);
    }

    public Object get(String s, Serializable serializable, LockOptions lockOptions) throws HibernateException {
        return getSession().get(s, serializable, lockOptions);
    }

    public String getEntityName(Object object) throws HibernateException {
        return getSession().getEntityName(object);
    }

    public IdentifierLoadAccess byId(String s) {
        return getSession().byId(s);
    }

    public IdentifierLoadAccess byId(Class aClass) {
        return getSession().byId(aClass);
    }

    public NaturalIdLoadAccess byNaturalId(String s) {
        return getSession().byNaturalId(s);
    }

    public NaturalIdLoadAccess byNaturalId(Class aClass) {
        return getSession().byNaturalId(aClass);
    }

    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String s) {
        return getSession().bySimpleNaturalId(s);
    }

    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class aClass) {
        return getSession().bySimpleNaturalId(aClass);
    }

    public Filter enableFilter(String filterName) {
        return getSession().enableFilter(filterName);
    }

    public Filter getEnabledFilter(String filterName) {
        return getSession().getEnabledFilter(filterName);
    }

    public void disableFilter(String filterName) {
        getSession().disableFilter(filterName);
    }

    public SessionStatistics getStatistics() {
        return getSession().getStatistics();
    }

    public boolean isReadOnly(Object o) {
        return getSession().isReadOnly(o);
    }

    public void setReadOnly(Object entity, boolean readOnly) {
        getSession().setReadOnly(entity, readOnly);
    }

    public void doWork(Work work) throws HibernateException {
        getSession().doWork(work);
    }

    public <T> T doReturningWork(ReturningWork<T> tReturningWork) throws HibernateException {
        return getSession().doReturningWork(tReturningWork);
    }

    public Connection disconnect() throws HibernateException {
        return getSession().disconnect();
    }

    public void reconnect(Connection connection) throws HibernateException {
        getSession().reconnect(connection);
    }

    public boolean isFetchProfileEnabled(String s) throws UnknownProfileException {
        return getSession().isFetchProfileEnabled(s);
    }

    public void enableFetchProfile(String s) throws UnknownProfileException {
        getSession().enableFetchProfile(s);
    }

    public void disableFetchProfile(String s) throws UnknownProfileException {
        getSession().disableFetchProfile(s);
    }

    public TypeHelper getTypeHelper() {
        return getSession().getTypeHelper();
    }

    public LobHelper getLobHelper() {
        return getSession().getLobHelper();
    }

    
    public void addEventListeners(SessionEventListener... listeners) {
        getSession().addEventListeners(listeners);
    }

    /**
     * Returns the delegated Session. If the persistence context is transaction-scoped, the Session associated with the current transaction will be used.
     *
     * @return the Session
     */
    private Session getSession() {
        // a transaction-scoped persistence context
        try {
            Transaction trx = tm.getTransaction();
            if (trx == null) {
                throw new IllegalStateException("A transaction is not active - ensure the component is executing in a managed transaction");
            }
            EntityManager em = emService.getEntityManager(unitName, this, trx);
            return (Session) em.getDelegate();
        } catch (SystemException | ContainerException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void clearEntityManager() {

    }
}