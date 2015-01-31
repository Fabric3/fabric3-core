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
import javax.persistence.EntityManagerFactory;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.host.ContainerException;
import org.fabric3.jpa.runtime.emf.EntityManagerFactoryCache;
import org.oasisopen.sca.annotation.Reference;

/**
 * Manages a cache of EntityManagers.
 *
 * EntityManager instances (and their underlying Hibernate Sessions) are cached for the duration of the associated JTA transaction and closed when the
 * transaction commits or rolls back.
 */
public class EntityManagerServiceImpl implements EntityManagerService {
    // a cache of entity managers keyed by Transaction and persistence unit name
    private Map<Key, EntityManager> cache = new ConcurrentHashMap<>();
    // tracks which entity managers have joined transactions
    private EntityManagerFactoryCache emfCache;

    public EntityManagerServiceImpl(@Reference EntityManagerFactoryCache emfCache) {
        this.emfCache = emfCache;
    }

    public EntityManager getEntityManager(String unitName, HibernateProxy proxy, Transaction transaction) throws ContainerException {
        // Note this method is thread-safe as a Transaction is only visible to a single thread at time.
        Key key = new Key(transaction, unitName);
        EntityManager em = cache.get(key);
        if (em == null) {
            // no entity manager for the persistence unit associated with the transaction
            EntityManagerFactory emf = emfCache.get(unitName);
            if (emf == null) {
                throw new ContainerException("No EntityManagerFactory found for persistence unit: " + unitName);
            }
            em = emf.createEntityManager();
            // don't synchronize on the transaction since it can assume to be bound to a thread at this point
            registerTransactionScopedSync(proxy, key);
            cache.put(key, em);
        }
        return em;
    }

    private void registerTransactionScopedSync(HibernateProxy proxy, Key key) throws ContainerException {
        try {
            TransactionScopedSync sync = new TransactionScopedSync(key, proxy);
            key.transaction.registerSynchronization(sync);
        } catch (RollbackException | SystemException e) {
            throw new ContainerException(e);
        }
    }

    /**
     * Callback used with a transaction-scoped EntityManager to remove it from the cache and close it.
     */
    private class TransactionScopedSync implements Synchronization {
        private Key key;
        private HibernateProxy proxy;

        private TransactionScopedSync(Key key, HibernateProxy proxy) {
            this.key = key;
            this.proxy = proxy;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            proxy.clearEntityManager();
            EntityManager manager = cache.remove(key);
            manager.close();
        }
    }

    private class Key {
        private Transaction transaction;
        private String unitName;

        private Key(Transaction transaction, String unitName) {
            this.transaction = transaction;
            this.unitName = unitName;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return !(transaction != null ? !transaction.equals(key.transaction) : key.transaction != null) && !(unitName != null ? !unitName.equals(
                    key.unitName) : key.unitName != null);

        }

        public int hashCode() {
            int result = transaction != null ? transaction.hashCode() : 0;
            result = 31 * result + (unitName != null ? unitName.hashCode() : 0);
            return result;
        }
    }
}
