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

import java.io.Serializable;
import java.sql.Connection;
import javax.persistence.EntityManager;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.UnknownProfileException;
import org.hibernate.jdbc.Work;
import org.hibernate.stat.SessionStatistics;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * An Hibernate Session proxy that delegates to a cached instance. This proxy is injected on stateless-scoped components. This proxy is
 * <strong>not</strong> safe to inject on composite-scoped implementations.
 * <p/>
 * If the persistence context is transaction-scoped (as defined by JPA), the proxy will attempt to retrieve the Session instance associated with the
 * current transaction context from the EntityManagerService. The proxy will cache the Session instance until the transaction completes (or aborts).
 */
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

    public void persist(Object entity) {
        initSession();
        session.persist(entity);
    }

    public void flush() {
        initSession();
        session.flush();
    }

    public EntityMode getEntityMode() {
        initSession();
        return session.getEntityMode();
    }

    public Session getSession(EntityMode entityMode) {
        initSession();
        return session.getSession(entityMode);
    }

    public void setFlushMode(FlushMode flushMode) {
        initSession();
        session.setFlushMode(flushMode);
    }

    public FlushMode getFlushMode() {
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

    public Connection connection() throws HibernateException {
        initSession();
        return session.connection();
    }

    public Connection close() throws HibernateException {
        initSession();
        return session.close();
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

    public void refresh(Object object, LockMode lockMode) throws HibernateException {
        initSession();
        session.refresh(object, lockMode);
    }

    public void refresh(Object o, LockOptions lockOptions) throws HibernateException {
        initSession();
        session.refresh(o, lockOptions);
    }

    public LockMode getCurrentLockMode(Object object) throws HibernateException {
        initSession();
        return session.getCurrentLockMode(object);
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

    public org.hibernate.Query createQuery(String queryString) throws HibernateException {
        initSession();
        return session.createQuery(queryString);
    }

    public SQLQuery createSQLQuery(String queryString) throws HibernateException {
        initSession();
        return session.createSQLQuery(queryString);
    }

    public org.hibernate.Query createFilter(Object collection, String queryString) throws HibernateException {
        initSession();
        return session.createFilter(collection, queryString);
    }

    public org.hibernate.Query getNamedQuery(String queryName) throws HibernateException {
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

    public Connection disconnect() throws HibernateException {
        initSession();
        return session.disconnect();
    }

    public void reconnect() throws HibernateException {
        initSession();
        session.reconnect();
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

    public void clearEntityManager() {
        session = null;
    }

    /**
     * Initializes the delegated Session. If the persistence context is transaction-scoped, the Session associated with the current transaction will
     * be used.
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
        } catch (SystemException e) {
            throw new ServiceRuntimeException(e);
        } catch (EntityManagerCreationException e) {
            throw new ServiceRuntimeException(e);
        }

    }

}