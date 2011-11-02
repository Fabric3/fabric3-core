/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
 *
 * @version $Rev$ $Date$
 */
public class EntityManagerServiceImpl implements EntityManagerService {
    // a cache of entity managers double keyed by scope and persistence unit name
    private Map<Object, Map<String, EntityManager>> cache = new ConcurrentHashMap<Object, Map<String, EntityManager>>();
    // tracks which entity managers have joined transactions
    private EntityManagerFactoryCache emfCache;

    public EntityManagerServiceImpl(@Reference EntityManagerFactoryCache emfCache) {
        this.emfCache = emfCache;
    }

    public EntityManager getEntityManager(String unitName, HibernateProxy proxy, Transaction transaction) throws EntityManagerCreationException {
        // Note this method is thread-safe as a Transaction is only visible to a single thread at time.
        EntityManager em = null;
        Map<String, EntityManager> map = cache.get(transaction);
        if (map != null) {
            em = map.get(unitName);
        }

        if (em == null) {
            // no entity manager for the persistence unit associated with the transaction
            EntityManagerFactory emf = emfCache.get(unitName);
            if (emf == null) {
                throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
            }
            em = emf.createEntityManager();
            // don't synchronize on the transaction since it can assume to be bound to a thread at this point
            registerTransactionScopedSync(proxy, unitName, transaction);
            if (map == null) {
                map = new ConcurrentHashMap<String, EntityManager>();
                cache.put(transaction, map);
            }
            map.put(unitName, em);
        }
        return em;
    }

    private void registerTransactionScopedSync(HibernateProxy proxy, String unitName, Transaction transaction)
            throws EntityManagerCreationException {
        try {
            TransactionScopedSync sync = new TransactionScopedSync(proxy, unitName, transaction);
            transaction.registerSynchronization(sync);
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
        private String unitName;
        private Transaction transaction;
        private HibernateProxy proxy;

        private TransactionScopedSync(HibernateProxy proxy, String unitName, Transaction transaction) {
            this.unitName = unitName;
            this.transaction = transaction;
            this.proxy = proxy;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            proxy.clearEntityManager();
            Map<String, EntityManager> map = cache.get(transaction);
            assert map != null;
            EntityManager manager = map.remove(unitName);
            manager.close();
            if (map.isEmpty()) {
                cache.remove(transaction);
            }
        }
    }

}
