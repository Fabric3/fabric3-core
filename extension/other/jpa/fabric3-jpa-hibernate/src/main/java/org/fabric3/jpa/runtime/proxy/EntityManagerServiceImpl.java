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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.runtime.emf.EntityManagerFactoryCache;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.invocation.F3Conversation;

/**
 * Implementation that manages a cache of EntityManagers.
 *
 * @version $Rev$ $Date$
 */
public class EntityManagerServiceImpl implements EntityManagerService {
    public static final Object JOINED = new Object();
    // a cache of entity managers double keyed by scope (transaction or conversation) and persistence unit name
    private Map<Object, Map<String, EntityManager>> cache = new ConcurrentHashMap<Object, Map<String, EntityManager>>();
    // tracks which entity managers have joined transactions
    private Map<Transaction, Object> joinedTransaction = new ConcurrentHashMap<Transaction, Object>();
    private EntityManagerFactoryCache emfCache;
    private TransactionManager tm;
    private ScopeContainer scopeContainer;

    public EntityManagerServiceImpl(@Reference EntityManagerFactoryCache emfCache, @Reference TransactionManager tm, @Reference ScopeRegistry registry) {
        this.emfCache = emfCache;
        this.tm = tm;
        this.scopeContainer = registry.getScopeContainer(Scope.CONVERSATION);
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

    public EntityManager getEntityManager(String unitName, HibernateProxy proxy, F3Conversation conversation)
            throws EntityManagerCreationException {
        // synchronize on the conversation since multiple request threads may be active
        synchronized (conversation) {
            EntityManager em = null;
            Map<String, EntityManager> map = cache.get(conversation);
            if (map != null) {
                em = map.get(unitName);
            }

            if (em == null) {
                // no entity manager for the persistence unit associated with the conversation
                try {
                    EntityManagerFactory emf = emfCache.get(unitName);
                    if (emf == null) {
                        throw new EntityManagerCreationException("No EntityManagerFactory found for persistence unit: " + unitName);
                    }
                    // don't synchronize on the transaction since it can assume to be bound to a thread at this point
                    em = emf.createEntityManager();
                    Transaction transaction = tm.getTransaction();
                    boolean mustJoin = !joinedTransaction.containsKey(transaction);
                    scopeContainer.registerCallback(conversation, new JPACallback(proxy, unitName, transaction, mustJoin));
                    // A transaction synchronization needs to be registered so that the proxy can clear the EM after the transaction commits.
                    // This is necessary so joinsTransaction is called for subsequent transactions
                    registerConversationScopedSync(proxy, transaction, mustJoin);
                    if (mustJoin) {
                        // join the current transaction. This only needs to be done for extended persistence conttexts
                        em.joinTransaction();
                        joinedTransaction.put(transaction, JOINED);
                    }
                    if (map == null) {
                        map = new ConcurrentHashMap<String, EntityManager>();
                        cache.put(conversation, map);
                    }
                    map.put(unitName, em);
                } catch (SystemException e) {
                    throw new EntityManagerCreationException(e);
                }
            }
            return em;
        }
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

    private void registerConversationScopedSync(HibernateProxy proxy, Transaction transaction, boolean joined)
            throws EntityManagerCreationException {
        try {
            ConversationScopedSync sync = new ConversationScopedSync(proxy, transaction, joined);
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
            map.remove(unitName);
            // TODO check that the JPA provider closes the EntityManager instance, since it is not closed here
            if (map.isEmpty()) {
                cache.remove(transaction);
            }
        }
    }

    /**
     * Callback used with a conversation-scoped EntityManager to clear out EM proxies when a transaction completes (necessary for join transaction).
     */
    private class ConversationScopedSync implements Synchronization {
        private Transaction transaction;
        private HibernateProxy proxy;
        private boolean joined;

        private ConversationScopedSync(HibernateProxy proxy, Transaction transaction, boolean joined) {
            this.transaction = transaction;
            this.proxy = proxy;
            this.joined = joined;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            if (joined) {
                joinedTransaction.remove(transaction);
            }
            proxy.clearEntityManager();
            // note the EM cache is not cleared here as it is done when the JPACallback is invoked at conversation end
        }
    }


    /**
     * Callback used with an extended persistence context EntityManager to remove it from the cache and close it.
     */
    private class JPACallback implements ConversationExpirationCallback {
        private HibernateProxy proxy;
        private String unitName;
        private Transaction transaction;
        private boolean joined;

        public JPACallback(HibernateProxy proxy, String unitName, Transaction transaction, boolean joined) {
            this.proxy = proxy;
            this.unitName = unitName;
            this.transaction = transaction;
            this.joined = joined;
        }

        public void expire(F3Conversation conversation) {
            synchronized (conversation) {
                if (joined) {
                    joinedTransaction.remove(transaction);
                }
                proxy.clearEntityManager();
                Map<String, EntityManager> map = cache.get(conversation);
                assert map != null;
                EntityManager em = map.remove(unitName);
                assert em != null;
                em.close();
                if (map.isEmpty()) {
                    cache.remove(conversation);
                }
            }
        }
    }
}
