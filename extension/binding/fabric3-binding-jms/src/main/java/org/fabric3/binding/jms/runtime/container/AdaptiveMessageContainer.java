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
package org.fabric3.binding.jms.runtime.container;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.fabric3.binding.jms.runtime.common.JmsHelper;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.binding.jms.spi.runtime.JmsConstants;

import static org.fabric3.binding.jms.spi.runtime.JmsConstants.CACHE_CONNECTION;
import static org.fabric3.binding.jms.spi.runtime.JmsConstants.CACHE_NONE;

/**
 * A container for a JMS MessageListener that is capable of adapting to varying workloads by dispatching messages from a destination to the listener
 * on different managed threads. Workload management is performed by sizing up or down the number of managed threads reserved for message processing.
 * <p/>
 * Note this implementation supports dispatching messages as part of a JTA transaction or non-transactionally.
 *
 * @version $Rev$ $Date$
 */
public class AdaptiveMessageContainer {
    private static final int DEFAULT_TRX_TIMEOUT = 30;

    private int cacheLevel = CACHE_CONNECTION;

    private int minReceivers = 1;
    private int maxReceivers = 1;
    private int idleLimit = 1;
    private int transactionTimeout = DEFAULT_TRX_TIMEOUT;
    private int receiveTimeout = transactionTimeout / 2;
    private int maxMessagesToProcess = -1;
    private long recoveryInterval = 5000;   // default 5 seconds
    private boolean durable = false;
    private boolean localDelivery;

    private TransactionType transactionType = TransactionType.NONE;
    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    private ConnectionFactory connectionFactory;
    private Destination destination;
    private String durableSubscriptionName;
    private String clientId;
    private String messageSelector;
    private MessageListener messageListener;
    private ExceptionListener exceptionListener;
    private Connection sharedConnection;

    // state information
    private boolean initialized = false;
    private boolean running = false;
    private boolean sharedConnectionStarted = false;
    private int activeReceiverCount = 0;

    // sync objects
    private final Object syncMonitor = new Object();
    protected final Object connectionSyncMonitor = new Object();
    private final Object recoverySyncMonitor = new Object();
    private Object recoveryMarker = new Object();

    private Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
    private List<Runnable> pausedWork = new LinkedList<Runnable>();

    private ExecutorService executorService;
    private TransactionManager tm;

    private MessageContainerMonitor monitor;

    /**
     * Constructor. Creates a new container for receiving messages from a destination and dispatching them to a MessageListener.
     *
     * @param destination       the destination
     * @param listener          the message listener
     * @param connectionFactory the connection factory to use for creating JMS resources
     * @param executorService   the work scheduler to schedule message receivers
     * @param tm                the JTA transaction manager for transacted messaging
     * @param monitor           the monitor for reporting events and errors
     */
    public AdaptiveMessageContainer(Destination destination,
                                    MessageListener listener,
                                    ConnectionFactory connectionFactory,
                                    ExecutorService executorService,
                                    TransactionManager tm,
                                    MessageContainerMonitor monitor) {
        this.executorService = executorService;
        this.destination = destination;
        this.messageListener = listener;
        this.durableSubscriptionName = listener.getClass().getName();
        this.connectionFactory = connectionFactory;
        this.tm = tm;
        this.monitor = monitor;
    }

    /**
     * Sets whether messages published by a connection are to be delivered to local topic subscribers.
     *
     * @param localDelivery true if messages published by a connection are to be delivered to local topic subscribers.
     */
    public void setLocalDelivery(boolean localDelivery) {
        this.localDelivery = localDelivery;
    }

    /**
     * Sets the timeout value for receiving messages from a destination. The default is no timeout.
     *
     * @param receiveTimeout the timeout value for receiving messages from a destination.
     */
    public void setReceiveTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Returns the timeout value for receiving messages from a destination. The default is no timeout.
     *
     * @return the timeout value for receiving messages from a destination.
     */
    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Sets the time in milliseconds to wait while making repeated recovery attempts.
     *
     * @param interval the time in milliseconds to wait
     */
    public void setRecoveryInterval(long interval) {
        this.recoveryInterval = interval;
    }

    /**
     * Sets the cache level for JMS artifacts. Currently {@link JmsConstants#CACHE_NONE}, {@link JmsConstants#CACHE_CONNECTION}, and {@link
     * JmsConstants#CACHE_SESSION} are supported. The default is to cache the connection.
     *
     * @param level the cache level
     */
    public void setCacheLevel(int level) {
        this.cacheLevel = level;
    }

    /**
     * Sets the minimum number of receivers to create for a destination. The default is one. Note the number of receivers for a topic should
     * gnenerally be one.
     *
     * @param min the minimum number of receivers to create.
     */
    public void setMinReceivers(int min) {
        synchronized (this.syncMonitor) {
            this.minReceivers = min;
            if (this.maxReceivers < min) {
                this.maxReceivers = min;
            }
        }
    }

    /**
     * Returns the minimum number of receivers to create for a destination.
     *
     * @return the minimum number of receivers to create for a destination
     */
    public int getMinReceivers() {
        synchronized (syncMonitor) {
            return minReceivers;
        }
    }

    /**
     * Sets the maximum threshold for the number of receivers to create for a destination. The default is one. Note the number of receivers for a
     * topic should generally be one.
     *
     * @param max the maximum threshold for the number of receivers to create for a destination
     */
    public void setMaxReceivers(int max) {
        synchronized (this.syncMonitor) {
            maxReceivers = (max > minReceivers ? max : minReceivers);
        }
    }

    /**
     * Returns the maximum threshold for the number of receivers to create for a destination.
     *
     * @return the maximum threshold for the number of receivers to create for a destination
     */
    public int getMaxReceivers() {
        synchronized (syncMonitor) {
            return maxReceivers;
        }
    }

    /**
     * Returns the number of scheduled receivers. This includes active and idle receivers.
     *
     * @return the number of scheduled receivers
     */
    public int getReceiverCount() {
        synchronized (syncMonitor) {
            return this.receivers.size();
        }
    }

    /**
     * Return the number of receivers actively processing messages.
     *
     * @return the number of receivers actively processing messages
     */
    public int getActiveReceiverCount() {
        synchronized (syncMonitor) {
            return this.activeReceiverCount;
        }
    }

    /**
     * Returns the number of paused receivers.
     *
     * @return the number of paused receivers
     */
    public int getPausedReceiversCount() {
        synchronized (syncMonitor) {
            return pausedWork.size();
        }
    }

    /**
     * Sets the number of times a receiver can be marked idle during its execution window before it is removed from the work scheduler. Note if the
     * maximum number of receivers is set to one, the single receiver will not be removed. Likewise, if a minimum number of receivers is set, idle
     * receivers will not be removed if that threshold is reached.
     *
     * @param limit the limit to set
     */
    public void setIdleLimit(int limit) {
        synchronized (syncMonitor) {
            this.idleLimit = limit;
        }
    }

    /**
     * Returns the maximum idle executions allowed for a receiver.
     *
     * @return the maximum idle executions allowed for a receiver
     */
    public int getIdleLimit() {
        synchronized (syncMonitor) {
            return idleLimit;
        }
    }

    /**
     * Sets the maximum number of messages to process by a receiver during its execution window. The default is unlimited, which results in the
     * receiver processing messages until shutdown. Setting the number lower increases the ability of the work scheduler to adjust scheduling at the
     * expense of thread context switches as receivers may be scheduled on different threads.
     *
     * @param max the maximum number of messages to process by a receivers
     */
    public void setMaxMessagesToProcess(int max) {
        synchronized (this.syncMonitor) {
            this.maxMessagesToProcess = max;
        }
    }

    /**
     * Return the maximum number of messages to process by a receiver.
     *
     * @return the maximum number of messages to process by a receiver
     */
    public int getMaxMessagesToProcess() {
        synchronized (this.syncMonitor) {
            return this.maxMessagesToProcess;
        }
    }

    /**
     * Sets the JMS message selector for message listening.
     *
     * @param selector the message selector
     */
    public void setMessageSelector(String selector) {
        this.messageSelector = selector;
    }

    /**
     * Returns the JMS message selector for message listening.
     *
     * @return the JMS message selector for message listening
     */
    public String getMessageSelector() {
        return this.messageSelector;
    }

    /**
     * Set the ExceptionListener for handling JMSExceptions during message processing.
     *
     * @param listener the ExceptionListener
     */
    public void setExceptionListener(ExceptionListener listener) {
        this.exceptionListener = listener;
    }

    /**
     * Sets whether durable topic subscriptions will be used.
     *
     * @param durable true if durable topic subscriptions will be used
     */
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * Returns if durable topic subscriptions will be used.
     *
     * @return true if durable topic subscriptions will be used
     */
    public boolean isDurable() {
        return this.durable;
    }

    /**
     * Sets the name of a durable subscription.
     *
     * @param name the durable subscription  name
     */
    public void setDurableSubscriptionName(String name) {
        this.durableSubscriptionName = name;
    }

    /**
     * Returns the name of the durable subscription.
     *
     * @return the name of the durable subscription or null if none is set
     */
    public String getDurableSubscriptionName() {
        return this.durableSubscriptionName;
    }

    /**
     * Sets the transaction mode to use: global, local (session), or none.
     *
     * @param transactionType the transaction mode to use
     */
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Sets the JMS message acknowledge mode to use.
     *
     * @param mode the JMS message acknowledge mode to use
     */
    public void setAcknowledgeMode(int mode) {
        this.acknowledgeMode = mode;
    }

    /**
     * Sets the JMS client id for a shared connection.
     *
     * @param id the id
     */
    public void setClientId(String id) {
        this.clientId = id;
    }

    /**
     * Returns the JMS client id.
     *
     * @return the id or null
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Returns true if the container is active.
     *
     * @return true if the container is active
     */
    public boolean isInitialized() {
        synchronized (this.syncMonitor) {
            return this.initialized;
        }
    }

    /**
     * Returns true if the container is running.
     *
     * @return if the container is running
     */
    public boolean isRunning() {
        synchronized (this.syncMonitor) {
            return this.running;
        }
    }

    /**
     * Initializes and starts the container. Once started, messages will be received.
     *
     * @throws JMSException if an initialization error occurs
     */
    public void initialize() throws JMSException {
        try {
            synchronized (this.syncMonitor) {
                initialized = true;
                syncMonitor.notifyAll();
            }
            start();
            synchronized (this.syncMonitor) {
                for (int i = 0; i < minReceivers; i++) {
                    addReceiver();
                }
            }
        } catch (JMSException e) {
            synchronized (connectionSyncMonitor) {
                JmsHelper.closeQuietly(sharedConnection);
                sharedConnection = null;
            }
            throw e;
        }
    }

    /**
     * Starts the container. Once started, messages will be received.
     *
     * @throws JMSException if an error during startup occurs
     */
    public void start() throws JMSException {
        if (cacheLevel >= CACHE_CONNECTION) {
            getSharedConnection();
        }

        synchronized (syncMonitor) {
            this.running = true;
            this.syncMonitor.notifyAll();
            resumePausedWork();
        }

        if (cacheLevel >= CACHE_CONNECTION) {
            startSharedConnection();
        }
    }

    /**
     * Stops the container from processing messages.
     *
     * @throws JMSException if an error occurs during stop
     */
    public void stop() throws JMSException {
        synchronized (syncMonitor) {
            this.running = false;
            this.syncMonitor.notifyAll();
        }

        if (cacheLevel >= CACHE_CONNECTION) {
            stopSharedConnection();
        }
    }

    /**
     * Stops the container from receiving message and releases all resources and receivers.
     */
    public void shutdown() {
        boolean wasRunning;
        synchronized (syncMonitor) {
            wasRunning = running;
            running = false;
            initialized = false;
            syncMonitor.notifyAll();
        }

        if (wasRunning && cacheLevel >= CACHE_CONNECTION) {
            stopSharedConnection();
        }
        try {
            synchronized (syncMonitor) {
                // wait for active receivers to finish processing
                while (activeReceiverCount > 0) {
                    syncMonitor.wait();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (cacheLevel >= CACHE_CONNECTION) {
                synchronized (connectionSyncMonitor) {
                    JmsHelper.closeQuietly(sharedConnection);
                    sharedConnection = null;
                }
            }
        }
    }


    /**
     * Resizes the receivers pool. If there are no idle receivers and the maximum number of receivers has not been reached, a new receiver will be
     * scheduled.
     */
    private void resizePool() {
        if (isRunning()) {
            resumePausedWork();
            synchronized (syncMonitor) {
                if (receivers.size() < maxReceivers && getIdleCount() == 0) {
                    addReceiver();
                    monitor.increaseReceivers(receivers.size());
                }
            }
        }
    }

    /**
     * Instantiate and schedules a new receiver.
     */
    private void addReceiver() {
        MessageReceiver receiver = new MessageReceiver();
        if (rescheduleWork(receiver)) {
            this.receivers.add(receiver);
        }
    }


    /**
     * Determines if the a receiver with the given idle count should be rescheduled.
     *
     * @param count the number of consecutive idle executions the receiver has accumulated
     * @return true if the receiver should be rescheduled
     */
    private boolean shouldRescheduleReceiver(int count) {
        boolean extra = (count >= idleLimit && getIdleCount() > 1);
        return (receivers.size() <= (extra ? minReceivers : maxReceivers));
    }

    /**
     * Returns the current number of idle receivers.
     *
     * @return the current number of idle receivers
     */
    private int getIdleCount() {
        int count = 0;
        for (MessageReceiver receiver : receivers) {
            if (receiver.isIdle()) {
                count++;
            }
        }
        return count;
    }


    /**
     * Refreshes the shared connection.
     *
     * @throws JMSException there is an error refreshing the connection
     */
    private void refreshSharedConnection() throws JMSException {
        synchronized (connectionSyncMonitor) {
            JmsHelper.closeQuietly(sharedConnection);
            sharedConnection = createSharedConnection();
            if (sharedConnectionStarted) {
                sharedConnection.start();
            }
        }
    }

    /**
     * Returns a shared connection
     *
     * @return the shared connection
     * @throws JMSException if there was an error returning the shared connection
     */
    private Connection getSharedConnection() throws JMSException {
        synchronized (connectionSyncMonitor) {
            if (sharedConnection == null) {
                sharedConnection = createSharedConnection();
            }
            return sharedConnection;
        }
    }


    /**
     * Create a shared connection.
     *
     * @return the connection
     * @throws JMSException if an error is encountered creating the connection
     */
    private Connection createSharedConnection() throws JMSException {
        Connection connection = connectionFactory.createConnection();
        try {
            String clientId = getClientId();
            if (clientId != null) {
                connection.setClientID(clientId);
            }
            return connection;
        }
        catch (JMSException ex) {
            JmsHelper.closeQuietly(connection);
            throw ex;
        }
    }

    /**
     * Starts a shared connection.
     */
    private void startSharedConnection() {
        try {
            synchronized (connectionSyncMonitor) {
                this.sharedConnectionStarted = true;
                if (sharedConnection != null) {
                    sharedConnection.start();
                }
            }
        } catch (JMSException e) {
            monitor.debugError("Error starting connection", e);
        }
    }

    /**
     * Stops a shared connection.
     */
    private void stopSharedConnection() {
        try {
            synchronized (connectionSyncMonitor) {
                sharedConnectionStarted = false;
                if (sharedConnection != null) {
                    sharedConnection.stop();
                }
            }
        } catch (Exception e) {
            monitor.error("Error stopping connection", e);
        }
    }

    /**
     * Refreshes a connection.
     */
    private void refreshConnection() {
        // loop until a connection has been obtained
        while (isRunning()) {
            try {
                if (cacheLevel >= CACHE_CONNECTION) {
                    refreshSharedConnection();
                } else {
                    Connection con = connectionFactory.createConnection();
                    JmsHelper.closeQuietly(con);
                }
                break;
            } catch (Exception e) {
                monitor.error("Error refreshing connection for destination: " + destination, e);
            }
            // wait and try again
            sleep();
        }
    }

    /**
     * Handle the given exception during a receive by delegating to an excetpion listener if the exception is a JMSException or sending it to the
     * monitor.
     *
     * @param e the exception to handle
     */
    private void handleReceiveException(Throwable e) {
        if (e instanceof JMSException) {
            if (exceptionListener != null) {
                exceptionListener.onException((JMSException) e);
            }
        }
        monitor.error("Listener threw an exception", e);
    }

    /**
     * Attempt to reschedule the given work. This is done immediately if the container is running, or deferred until restart. If the container is
     * shutdown, scheduling will not be performed.
     *
     * @param runnable the work to reschedule
     * @return true if the work has been rescheduled
     */
    private boolean rescheduleWork(Runnable runnable) {
        if (isRunning()) {
            try {
                executorService.execute(runnable);
            } catch (RuntimeException e) {
                monitor.reject(e);
                pausedWork.add(runnable);
            }
            return true;
        } else if (initialized) {
            this.pausedWork.add(runnable);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempts to reschedule paused work.
     */
    private void resumePausedWork() {
        synchronized (syncMonitor) {
            if (!pausedWork.isEmpty()) {
                for (Iterator<Runnable> it = pausedWork.iterator(); it.hasNext();) {
                    Runnable runnable = it.next();
                    try {
                        executorService.execute(runnable);
                        it.remove();
                    }
                    catch (RuntimeException e) {
                        // keep the work paused paused and log the event
                        monitor.reject(e);
                    }
                }
            }
        }
    }

    /**
     * Sleep according to the specified recovery interval.
     */
    private void sleep() {
        if (this.recoveryInterval > 0) {
            try {
                Thread.sleep(recoveryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Performs a local commit or acknowledgement if a local transaction or client acknowledgement is being used.
     *
     * @param session the session to commit
     * @param message the message to acknowledge
     * @throws JMSException if the commit fails
     */
    private void localCommitOrAcknowledge(Session session, Message message) throws JMSException {
        if (TransactionType.SESSION == transactionType) {
            session.commit();
        } else if (Session.CLIENT_ACKNOWLEDGE == session.getAcknowledgeMode()) {
            message.acknowledge();
        }
    }

    /**
     * Commits the current global (JTA) transaction.
     *
     * @throws TransactionException if a commit error was encountered
     */
    private void globalCommit() throws TransactionException {
        try {
            if (tm.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                tm.commit();
            } else {
                tm.rollback();
            }
        } catch (SystemException e) {
            throw new TransactionException(e);
        } catch (IllegalStateException e) {
            throw new TransactionException(e);
        } catch (SecurityException e) {
            throw new TransactionException(e);
        } catch (HeuristicMixedException e) {
            throw new TransactionException(e);
        } catch (HeuristicRollbackException e) {
            throw new TransactionException(e);
        } catch (RollbackException e) {
            throw new TransactionException(e);
        }
    }

    /**
     * Rollbacks the current global (JTA) transaction.
     *
     * @throws TransactionException if an error rolling back was encountered
     */
    private void globalRollback() throws TransactionException {
        try {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
            }
        } catch (SystemException e) {
            throw new TransactionException(e);
        }
    }

    /**
     * Performs a local rollback of the JMS session.
     *
     * @param session the session to rollback
     * @throws JMSException if the rollback fails
     */
    private void localRollback(Session session) throws JMSException {
        if (TransactionType.SESSION == transactionType) {
            session.rollback();
        }
    }

    /**
     * Creates a session.
     *
     * @param connection the connection to use
     * @return the session
     * @throws JMSException if there is an error creating the session
     */
    private Session createSession(Connection connection) throws JMSException {
        boolean transacted = TransactionType.SESSION == transactionType || TransactionType.GLOBAL == transactionType;
        // FIXME Atomikos requires this set to "true" but app servers (and Java EE) requires it to be false for XA transactions
        return connection.createSession(transacted, acknowledgeMode);
    }

    /**
     * Creates a MessageConsumer for the given destination and session.
     *
     * @param destination the destination
     * @param session     the session
     * @return the consumer
     * @throws JMSException if an error is encountered creating the consumer
     */
    private MessageConsumer createConsumer(Destination destination, Session session) throws JMSException {
        if (destination instanceof Topic && !(destination instanceof Queue)) {
            if (isDurable()) {
                return session.createDurableSubscriber((Topic) destination, getDurableSubscriptionName(), getMessageSelector(), localDelivery);
            } else {
                return session.createConsumer(destination, getMessageSelector(), localDelivery);
            }
        } else {
            return session.createConsumer(destination, getMessageSelector());
        }
    }


    /**
     * Listens for messages from a destination and dispatches them to a message listener, managing transaction semantics and recovery if necessary.
     */
    private class MessageReceiver implements Runnable {
        private Session session;
        private MessageConsumer consumer;
        private Object previousRecoveryMarker;
        private boolean previousSucceeded;
        private int idleWorkCount = 0;
        private volatile boolean idle = true;

        public boolean isIdle() {
            return idle;
        }

        public void run() {
            synchronized (syncMonitor) {
                activeReceiverCount++;
                syncMonitor.notifyAll();
            }
            boolean messageReceived = false;
            try {
                if (maxMessagesToProcess < 0) {
                    messageReceived = receiveLoop();
                } else {
                    int messageCount = 0;
                    while (isRunning() && messageCount < maxMessagesToProcess) {
                        messageReceived = (receiveMessage() || messageReceived);
                        messageCount++;
                    }
                }
            } catch (Throwable e) {
                closeResources();
                if (!previousSucceeded) {
                    // consecutive failure - wait to retry
                    sleep();
                }
                previousSucceeded = false;
                handleReceiveException(e);
                synchronized (recoverySyncMonitor) {
                    if (previousRecoveryMarker == recoveryMarker) {
                        refreshConnection();
                        recoveryMarker = new Object();
                    }
                }
            }
            synchronized (syncMonitor) {
                activeReceiverCount--;
                syncMonitor.notifyAll();
            }
            if (!messageReceived) {
                idleWorkCount++;
            } else {
                idleWorkCount = 0;
            }
            synchronized (syncMonitor) {
                // attempt to reschedule this receiver
                if (!shouldRescheduleReceiver(idleWorkCount) || !rescheduleWork(this)) {
                    // shutdown this receiver as it should not be rescheduled or the reschedule failed
                    receivers.remove(this);
                    monitor.decreaseReceivers(receivers.size());
                    syncMonitor.notifyAll();
                    closeResources();
                } else if (isRunning()) {
                    int nonPausedReceivers = getReceiverCount() - getPausedReceiversCount();
                    if (nonPausedReceivers < 1) {
                        monitor.errorMessage("All receivers are paused, possibly as a result of rejected work.");
                    } else if (nonPausedReceivers < getMinReceivers()) {
                        monitor.errorMessage("The number is below the minimum threshold, possibly as a result of rejected work.");
                    }
                }
            }
        }

        /**
         * Loops while the container is running, receiving and dispatching messages.
         *
         * @return true if a message was received on executing the loop
         * @throws JMSException         if an error occurs processing a message
         * @throws TransactionException if receiving a globally transacted message and a transaction operation (begin, commit, rollback) fails.
         */
        private boolean receiveLoop() throws JMSException, TransactionException {
            boolean received = false;
            boolean active = true;
            while (active) {
                synchronized (syncMonitor) {
                    boolean interrupted = false;
                    boolean waiting = false;
                    while ((active = isInitialized()) && !isRunning()) {
                        if (interrupted) {
                            throw new IllegalStateException("Interrupted while waiting for restart");
                        }
                        if (!isRunning()) {
                            return false;
                        }
                        if (!waiting && isRunning()) {
                            activeReceiverCount--;
                        }
                        waiting = true;
                        try {
                            syncMonitor.wait();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            interrupted = true;
                        }
                    }
                    if (waiting) {
                        activeReceiverCount++;
                    }
                }
                if (active) {
                    received = (receiveMessage() || received);
                }
            }
            return received;
        }

        /**
         * Waits to receive a single message. If a message is received in the configured timeframe, it is dispatched to the listener.
         *
         * @return true if a message was received
         * @throws JMSException         if there was an error receiving the message
         * @throws TransactionException if receiving a globally transacted message and a transaction operation (begin, commit, rollback) fails.
         */
        private boolean receiveMessage() throws JMSException, TransactionException {
            setRecoveryMarker();
            boolean received;
            if (TransactionType.GLOBAL == transactionType) {
                received = jtaReceiveMessage();
            } else {
                received = receive();
            }
            this.previousSucceeded = true;
            return received;
        }

        /**
         * Waits to receive a message and invokes the listener within the context of a global transaction.
         *
         * @return true if a message was received
         * @throws JMSException         if an exception occured during the receive
         * @throws TransactionException if an exception starting, committing, or rolling back a transaction occured
         */
        private boolean jtaReceiveMessage() throws JMSException, TransactionException {

            try {
                int status = tm.getStatus();
                boolean begun = false;
                if (Status.STATUS_NO_TRANSACTION == status) {
                    // this should always be true
                    tm.begin();
                    begun = true;
                }
                boolean received;
                received = receive();
                if (begun) {
                    globalCommit();
                }
                return received;
            } catch (JMSException e) {
                monitor.error("Error receiving message", e);
                globalRollback();
            } catch (RuntimeException e) {
                monitor.error("Error receiving message", e);
                globalRollback();
            } catch (Error e) {
                monitor.error("Error receiving message", e);
                globalRollback();
            } catch (SystemException e) {
                throw new TransactionException(e);
            } catch (NotSupportedException e) {
                throw new TransactionException(e);
            }
            return false;
        }

        /**
         * Waits to receive a message and invokes the listener.
         *
         * @return true if a message was received
         * @throws JMSException if an exception occurred during the receive
         */
        private boolean receive() throws JMSException {
            Connection connectionToUse = null;
            Session sessionToClose = null;
            MessageConsumer consumerToClose = null;
            try {
                Session sessionToUse = session;
                if (sessionToUse == null) {
                    if (cacheLevel >= CACHE_CONNECTION) {
                        connectionToUse = getSharedConnection();
                    } else {
                        connectionToUse = connectionFactory.createConnection();
                        connectionToUse.start();
                    }
                    sessionToUse = createSession(connectionToUse);
                    sessionToClose = sessionToUse;
                }
                MessageConsumer consumerToUse = consumer;
                if (consumerToUse == null) {
                    consumerToUse = createConsumer(destination, sessionToUse);
                    consumerToClose = consumerToUse;
                }
                int timeout = receiveTimeout;
                if (TransactionType.GLOBAL == transactionType) {
                    if (timeout == 0) {
                        // No default receive timeout was defined, which means the consumer will block indefinitely. If JTA transaction is begun, this
                        // can result in a transaction timeout before the message is received. Set the receive timeout to half that of the transaction
                        // timeout to avoid this.
                        timeout = transactionTimeout / 2;
                    }
                    try {
                        tm.setTransactionTimeout(transactionTimeout);
                    } catch (SystemException e) {
                        monitor.error("Error setting transaction timeout", e);
                        return false;
                    }
                }
                // wait for a message, blocking for the timeout period, which, if 0, will be indefinitely
                Message message = consumerToUse.receive(timeout);
                if (message != null) {
                    if (!isRunning()) {
                        // container is shutting down.
                        if (TransactionType.GLOBAL == transactionType) {
                            try {
                                tm.rollback();
                            } catch (SystemException e) {
                                monitor.error("Error setting rollback", e);
                            }
                        } else {
                            localRollback(session);
                        }
                        idle = true;
                        return false;
                    }

                    idle = false;
                    resizePool();
                    try {
                        messageListener.onMessage(message);
                        localCommitOrAcknowledge(sessionToUse, message);
                    } catch (JMSException e) {
                        if (TransactionType.SESSION == transactionType) {
                            localRollback(sessionToUse);
                        }
                        throw e;
                    } catch (RuntimeException e) {
                        if (TransactionType.SESSION == transactionType) {
                            localRollback(sessionToUse);
                        }
                        throw e;
                    }
                    return true;
                } else {
                    idle = true;
                    return false;
                }
            }
            finally {
                JmsHelper.closeQuietly(consumerToClose);
                JmsHelper.closeQuietly(sessionToClose);
                if (cacheLevel == CACHE_NONE) {
                    JmsHelper.closeQuietly(connectionToUse);
                }
            }
        }

        private void closeResources() {
            synchronized (connectionSyncMonitor) {
                JmsHelper.closeQuietly(consumer);
                JmsHelper.closeQuietly(session);
            }
            consumer = null;
            session = null;
        }

        private void setRecoveryMarker() {
            synchronized (recoverySyncMonitor) {
                previousRecoveryMarker = recoveryMarker;
            }
        }

    }

}
