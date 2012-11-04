/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.runtime.emf.EntityManagerFactoryCache;

/**
 * Manages a cache of EntityManagers.
 * <p/>
 * EntityManager instances (and their underlying Hibernate Sessions) are cached for the duration of the associated JTA transaction and closed when the
 * transaction commits or rolls back.
 */
public class EntityManagerServiceImpl implements EntityManagerService {
    // a cache of entity managers keyed by Transaction and persistence unit name
    private Map<Key, EntityManager> cache = new ConcurrentHashMap<Key, EntityManager>();
    // tracks which entity managers have joined transactions
    private EntityManagerFactoryCache emfCache;

    public EntityManagerServiceImpl(@Reference EntityManagerFactoryCache emfCache) {
        this.emfCache = emfCache;
    }

    public EntityManager getEntityManager(String unitName, HibernateProxy proxy, Transaction transaction) throws EntityManagerCreationException {
        // Note this method is thread-safe as a Transaction is only visible to a single thread at time.
        Key key = new Key(transaction, unitName);
        EntityManager em = cache.get(key);
        if (em == null) {
            // no entity manager for the persistence unit associated with the transaction
            EntityManagerFactory emf = emfCache.get(unitName);
            if (emf == null) {
                throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
            }
            em = emf.createEntityManager();
            // don't synchronize on the transaction since it can assume to be bound to a thread at this point
            registerTransactionScopedSync(proxy, key);
            cache.put(key, em);
        }
        return em;
    }

    private void registerTransactionScopedSync(HibernateProxy proxy, Key key) throws EntityManagerCreationException {
        try {
            TransactionScopedSync sync = new TransactionScopedSync(key, proxy);
            key.transaction.registerSynchronization(sync);
        } catch (RollbackException e) {
            throw new EntityManagerCreationException(e);
        } catch (SystemException e) {
            throw new EntityManagerCreationException(e);
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
