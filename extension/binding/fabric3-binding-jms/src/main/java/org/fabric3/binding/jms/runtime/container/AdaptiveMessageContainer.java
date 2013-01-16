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
package org.fabric3.binding.jms.runtime.container;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.binding.jms.runtime.common.JmsHelper;
import org.fabric3.binding.jms.spi.common.DestinationType;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.spi.threadpool.ExecutionContext;
import org.fabric3.spi.threadpool.ExecutionContextTunnel;

import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_ADMINISTERED_OBJECTS;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_CONNECTION;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_NONE;

/**
 * A container for a JMS MessageListener that is capable of adapting to varying workloads by dispatching messages from a destination to the listener
 * on different managed threads. Workload management is performed by sizing up or down the number of managed threads reserved for message processing.
 * <p/>
 * Note this implementation supports dispatching transactional and non-transactional messages.
 */
@Management
public class AdaptiveMessageContainer {
    private final ConnectionManager connectionManager;
    private UnitOfWork work;
    private ContainerStatistics statistics;
    private ExecutorService executorService;

    private MessageContainerMonitor monitor;

    // container configuration
    private int receiveTimeout;
    private URI listenerUri;
    private DestinationType destinationType;
    private Destination destination;
    private int cacheLevel;
    private TransactionType transactionType;
    private int minReceivers;
    private int maxReceivers;
    private int idleLimit;
    private int maxMessagesToProcess;
    private long recoveryInterval;
    private String subscriptionName;
    private boolean localDelivery;
    private String messageSelector;

    // listeners to receive incoming messages or errors
    private MessageListener messageListener;
    private ExceptionListener exceptionListener;

    // state information
    private boolean initialized;
    private boolean running;
    private int activeReceiverCount;

    // sync objects
    private final Object syncMonitor = new Object();
    private final Object recoverySyncMonitor = new Object();
    private Object recoveryMarker = new Object();

    private Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
    private List<Runnable> pausedWork = new LinkedList<Runnable>();
    private boolean javaEEXAEnabled;

    /**
     * Constructor. Creates a new container for receiving messages from a destination and dispatching them to a MessageListener.
     *
     * @param configuration     the container configuration
     * @param receiveTimeout    the timeout for receiving messages in seconds
     * @param connectionManager the connection manager
     * @param work              the unit of work
     * @param statistics        the message statistics tracker
     * @param executorService   the work scheduler to schedule message receivers
     * @param javaEEXAEnabled   true if the host is a Java EE XA-enabled container
     * @param monitor           the monitor for reporting events and errors
     */
    public AdaptiveMessageContainer(ContainerConfiguration configuration,
                                    int receiveTimeout,
                                    ConnectionManager connectionManager,
                                    UnitOfWork work,
                                    ContainerStatistics statistics,
                                    ExecutorService executorService,
                                    boolean javaEEXAEnabled,
                                    MessageContainerMonitor monitor) {
        listenerUri = configuration.getUri();
        destinationType = configuration.getDestinationType();
        destination = configuration.getDestination();
        cacheLevel = configuration.getCacheLevel();
        transactionType = configuration.getType();
        messageListener = configuration.getMessageListener();
        exceptionListener = configuration.getExceptionListener();
        messageSelector = configuration.getMessageSelector();

        setReceiveTimeout(receiveTimeout);
        setMaxMessagesToProcess(configuration.getMaxMessagesToProcess());
        setMaxReceivers(configuration.getMaxReceivers());
        setMinReceivers(configuration.getMinReceivers());
        setRecoveryInterval(configuration.getRecoveryInterval());
        setIdleLimit(configuration.getIdleLimit());

        setRecoveryInterval(configuration.getRecoveryInterval());

        // TODO additional configuration
        // container.setLocalDelivery();

        this.connectionManager = connectionManager;
        this.work = work;
        this.statistics = statistics;
        this.executorService = executorService;
        this.javaEEXAEnabled = javaEEXAEnabled;
        this.monitor = monitor;
    }

    /**
     * Sets the timeout value for receiving messages from a destination. The default is no timeout.
     *
     * @param timeout the timeout value for receiving messages from a destination.
     */
    @ManagementOperation(description = "The timeout value for receiving messages from a destination")
    public void setReceiveTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Receive timeout must be greater than 0");
        }
        receiveTimeout = timeout;
    }

    /**
     * Returns the timeout value for receiving messages from a destination. The default is no timeout.
     *
     * @return the timeout value for receiving messages from a destination.
     */
    @ManagementOperation(description = "The timeout value for receiving messages from a destination")
    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Sets the time in milliseconds to wait while making repeated recovery attempts.
     *
     * @param interval the time in milliseconds to wait
     */
    @ManagementOperation(description = "The time to wait while making repeated recovery attempts")
    public void setRecoveryInterval(long interval) {
        recoveryInterval = interval;
    }

    @ManagementOperation(description = "The time to wait while making repeated recovery attempts")
    public long getRecoveryInterval() {
        return recoveryInterval;
    }

    @ManagementOperation(description = "The cache level")
    public String getLevel() {
        if (cacheLevel == CACHE_CONNECTION) {
            return "Connection";
        } else if (cacheLevel == CACHE_ADMINISTERED_OBJECTS) {
            return "Administered Objects";
        } else {
            return "None";
        }
    }

    /**
     * Sets the minimum number of receivers to create for a destination. The default is one. Note the number of receivers for a topic should generally
     * be one.
     *
     * @param min the minimum number of receivers to create.
     */
    @ManagementOperation(description = "The minimum number of receivers to create for a destination")
    public void setMinReceivers(int min) {
        synchronized (syncMonitor) {
            minReceivers = min;
            if (maxReceivers < min) {
                maxReceivers = min;
            }
        }
    }

    /**
     * Returns the minimum number of receivers to create for a destination.
     *
     * @return the minimum number of receivers to create for a destination
     */
    @ManagementOperation(description = "The minimum number of receivers to create for a destination")
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
    @ManagementOperation(description = "The maximum number of receivers to create for a destination")
    public void setMaxReceivers(int max) {
        synchronized (syncMonitor) {
            maxReceivers = (max > minReceivers ? max : minReceivers);
        }
    }

    /**
     * Returns the maximum threshold for the number of receivers to create for a destination.
     *
     * @return the maximum threshold for the number of receivers to create for a destination
     */
    @ManagementOperation(description = "The maximum number of receivers to create for a destination")
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
    @ManagementOperation(description = "The number of scheduled receivers")
    public int getReceiverCount() {
        synchronized (syncMonitor) {
            return receivers.size();
        }
    }

    /**
     * Return the number of receivers actively processing messages.
     *
     * @return the number of receivers actively processing messages
     */
    @ManagementOperation(description = "The number of receivers actively processing messages")
    public int getActiveReceiverCount() {
        synchronized (syncMonitor) {
            return activeReceiverCount;
        }
    }

    /**
     * Returns the number of paused receivers.
     *
     * @return the number of paused receivers
     */
    @ManagementOperation(description = "The number of paused receivers")
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
    @ManagementOperation(description = "The times a receiver can be marked idle during execution before it is removed from the work scheduler")
    public void setIdleLimit(int limit) {
        synchronized (syncMonitor) {
            idleLimit = limit;
        }
    }

    /**
     * Returns the maximum idle executions allowed for a receiver.
     *
     * @return the maximum idle executions allowed for a receiver
     */
    @ManagementOperation(description = "The times a receiver can be marked idle during execution before it is removed from the work scheduler")
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
    @ManagementOperation(description = "The maximum number of messages to process by a receivers")
    public void setMaxMessagesToProcess(int max) {
        synchronized (syncMonitor) {
            maxMessagesToProcess = max;
        }
    }

    /**
     * Return the maximum number of messages to process by a receiver.
     *
     * @return the maximum number of messages to process by a receiver
     */
    @ManagementOperation(description = "The maximum number of messages to process by a receivers")
    public int getMaxMessagesToProcess() {
        synchronized (syncMonitor) {
            return maxMessagesToProcess;
        }
    }

    /**
     * Returns if durable topic subscriptions will be used.
     *
     * @return true if durable topic subscriptions will be used
     */
    @ManagementOperation(description = "If durable topic subscriptions are used")
    public boolean isDurable() {
        return connectionManager.isDurable();
    }

    /**
     * Returns the name of the durable subscription.
     *
     * @return the name of the durable subscription or null if none is set
     */
    @ManagementOperation(description = "The durable topic subscription name")
    public String getSubscriptionName() {
        return subscriptionName;
    }

    @ManagementOperation(description = "The transaction type")
    public String getTransactionType() {
        return transactionType.toString();
    }

    /**
     * Returns true if the container is active.
     *
     * @return true if the container is active
     */
    @ManagementOperation(description = "True if the container is initialized")
    public boolean isInitialized() {
        synchronized (syncMonitor) {
            return initialized;
        }
    }

    /**
     * Returns true if the container is running.
     *
     * @return if the container is running
     */
    @ManagementOperation(description = "True if the container is running")
    public boolean isRunning() {
        synchronized (syncMonitor) {
            return running;
        }
    }

    /**
     * Returns the current number of idle receivers.
     *
     * @return the current number of idle receivers
     */
    @ManagementOperation(description = "The current number of idle receivers")
    public int getIdleCount() {
        int count = 0;
        for (MessageReceiver receiver : receivers) {
            if (receiver.isIdle()) {
                count++;
            }
        }
        return count;
    }

    @ManagementOperation(description = "The time this container has been running")
    public long getTotalTime() {
        return statistics.getTotalTime();
    }

    @ManagementOperation(description = "The number of messages received")
    public long getMessagesReceived() {
        return statistics.getMessagesReceived();
    }

    @ManagementOperation(description = "The maximum number of active receivers reached")
    public int getMaxReceiversReached() {
        return statistics.getMaxReceivers();
    }

    @ManagementOperation(description = "The total number of committed transactions")
    public int getTransactions() {
        return statistics.getTransactions();
    }

    @ManagementOperation(description = "The total number of rolled back transactions")
    public int getTransactionsRolledBack() {
        return statistics.getTransactionsRolledBack();
    }

    /**
     * Starts the container. Once started, messages will be received.
     *
     * @throws JMSException if an error during start-up occurs
     */
    @ManagementOperation(description = "Starts the containing processing messages")
    public void start() throws JMSException {
        connectionManager.start();
        synchronized (syncMonitor) {
            running = true;
            syncMonitor.notifyAll();
            resumePausedWork();
        }

        if (cacheLevel >= CACHE_CONNECTION) {
            connectionManager.startSharedConnection();
        }
    }

    /**
     * Stops the container processing messages.
     *
     * @throws JMSException if an error occurs during stop
     */
    @ManagementOperation(description = "Stops the containing processing messages")
    public void stop() throws JMSException {
        synchronized (syncMonitor) {
            running = false;
            syncMonitor.notifyAll();
        }

        if (cacheLevel >= CACHE_CONNECTION) {
            connectionManager.stopSharedConnection();
        }
    }

    /**
     * Initializes and starts the container. Once started, messages will be received.
     *
     * @throws JMSException if an initialization error occurs
     */
    public void initialize() throws JMSException {
        if (isDurable()) {
            subscriptionName = "listenerSubscription";
        }
        try {
            synchronized (syncMonitor) {
                initialized = true;
                syncMonitor.notifyAll();
            }
            start();
            synchronized (syncMonitor) {
                for (int i = 0; i < minReceivers; i++) {
                    addReceiver();
                }
            }
        } catch (JMSException e) {
            connectionManager.close();
            throw e;
        }
    }

    /**
     * Stops the container from receiving message and releases all resources and receivers.
     *
     * @throws JMSException if there is an error during shutdown.
     */
    public void shutdown() throws JMSException {
        boolean wasRunning;
        synchronized (syncMonitor) {
            wasRunning = running;
            running = false;
            initialized = false;
            syncMonitor.notifyAll();
            if (wasRunning && cacheLevel >= CACHE_CONNECTION) {
                connectionManager.stopSharedConnection();
            }
            try {
                // wait for active receivers to finish processing
                while (activeReceiverCount > 0) {
                    syncMonitor.wait();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                if (cacheLevel >= CACHE_CONNECTION) {
                    connectionManager.close();
                }
            }
        }
    }

    /**
     * Re-sizes the receivers pool. If there are no idle receivers and the maximum number of receivers has not been reached, a new receiver will be
     * scheduled.
     */
    private void resizePool() {
        if (isRunning()) {
            resumePausedWork();
            synchronized (syncMonitor) {
                if (receivers.size() < maxReceivers && getIdleCount() == 0) {
                    addReceiver();
                }
            }
        }
    }

    /**
     * Instantiates and schedules a new receiver.
     */
    private void addReceiver() {
        MessageReceiver receiver = new MessageReceiver();
        if (rescheduleWork(receiver)) {
            receivers.add(receiver);
            if (statistics.getMaxReceivers() < receivers.size()) {
                statistics.incrementMaxReceivers();
            }
            monitor.increaseReceivers(receivers.size());
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
     * Refreshes a connection.
     */
    private void refreshConnection() {
        // loop until a connection has been obtained
        while (isRunning()) {
            if (connectionManager.refreshConnection()) {
                return;
            }
            // wait and try again
            sleep();
        }
    }

    /**
     * Handle the given exception during a receive by delegating to an exception listener if the exception is a JMSException or sending it to the
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
        monitor.listenerError(listenerUri.toString(), e);
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
            pausedWork.add(runnable);
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
                    } catch (RuntimeException e) {
                        // keep the work paused paused and log the event
                        monitor.reject(e);
                    }
                }
            }
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
        if (javaEEXAEnabled && TransactionType.GLOBAL == transactionType) {
            // Java EE containers requires require the transacted parameter to be set to false for XA transactions
            return connection.createSession(false, Session.SESSION_TRANSACTED);
        }
        // non-Java EE/XA environment (e.g. Atomikos, a local transaction or no transaction)
        boolean transacted = TransactionType.SESSION == transactionType || TransactionType.GLOBAL == transactionType;
        return connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Creates a MessageConsumer for the given destination and session.
     *
     * @param session the session
     * @return the consumer
     * @throws JMSException if an error is encountered creating the consumer
     */
    private MessageConsumer createConsumer(Session session) throws JMSException {
        if (DestinationType.TOPIC == destinationType) {
            if (isDurable()) {
                return session.createDurableSubscriber((Topic) destination, subscriptionName, messageSelector, localDelivery);
            } else {
                return session.createConsumer(destination, messageSelector, localDelivery);
            }
        } else {
            return session.createConsumer(destination, messageSelector);
        }
    }

    /**
     * Sleep according to the specified recovery interval.
     */
    private void sleep() {
        if (recoveryInterval > 0) {
            try {
                Thread.sleep(recoveryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Listens for messages from a destination and dispatches them to a message listener, managing transaction semantics and recovery if necessary.
     */
    private class MessageReceiver implements Runnable {
        private Connection connection;
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
                    monitor.scheduledReceiver(destination.toString());
                    messageReceived = receiveLoop();
                } else {
                    int messageCount = 0;
                    while (isRunning() && messageCount < maxMessagesToProcess) {
                        messageReceived = (receive() || messageReceived);
                        messageCount++;
                    }
                }
            } catch (Throwable e) {
                closeSession();
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
                    closeSession();
                } else if (isRunning()) {
                    int nonPausedReceivers = getReceiverCount() - getPausedReceiversCount();
                    if (nonPausedReceivers < 1) {
                        monitor.pauseError(listenerUri.toString());
                    } else if (nonPausedReceivers < getMinReceivers()) {
                        monitor.minimumError(listenerUri.toString());
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
                // reset the execution context so the thread does not appear stalled to the runtime
                ExecutionContext context = ExecutionContextTunnel.getThreadExecutionContext();

                synchronized (syncMonitor) {
                    try {
                        if (context != null) {
                            context.start();
                        }
                        boolean interrupted = false;
                        boolean waiting = false;
                        while ((active = isInitialized()) && !isRunning()) {
                            if (interrupted) {
                                throw new IllegalStateException("Interrupted while waiting for restart for " + listenerUri);
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
                        if (context != null) {
                            context.stop();
                        }
                    } finally {
                        if (context != null) {
                            context.clear();
                        }
                    }
                }
                if (active) {
                    received = (receive() || received);
                }
            }
            closeResources(true);
            return received;
        }

        /**
         * Waits to receive a single message. If a message is received in the configured timeframe, it is dispatched to the listener.
         *
         * @return true if a message was received
         * @throws JMSException         if there was an error receiving the message
         * @throws TransactionException if receiving a globally transacted message and a transaction operation (begin, commit, rollback) fails.
         */
        private boolean receive() throws JMSException, TransactionException {
            try {
                setRecoveryMarker();
                boolean received = doReceive();
                previousSucceeded = true;
                return received;
            } finally {
                closeResources(false);
            }
        }

        /**
         * Initiates a transaction context if required and performs the blocking receive on the JMS destination.
         *
         * @return true if a message was received
         * @throws JMSException         if a JMS-related exception occurred during the receive
         * @throws TransactionException if a transaction exception occurred during thr receive
         */
        private boolean doReceive() throws JMSException, TransactionException {
            synchronized (syncMonitor) {
                if (!isRunning()) {
                    return false;
                }
                work.begin();
                connection = connectionManager.getConnection();
            }
            if (session == null) {
                session = createSession(connection);
            }
            if (consumer == null) {
                consumer = createConsumer(session);
            }
            // wait for a message, blocking for the timeout period, which, if 0, will be indefinitely
            Message message = null;
            try {
                message = consumer.receive(receiveTimeout);
            } catch (JMSException e) {
                if (e.getCause() instanceof InterruptedException) {
                    // some providers may throw an InterruptedException if the receiver is blocking when the runtime is signalled to shutdown
                    // ignore the exception
                } else {
                    throw e;
                }
            }
            if (message != null) {
                if (!isRunning()) {
                    // container is shutting down.
                    work.rollback(session);
                    idle = true;
                    return false;
                }

                idle = false;
                resizePool();
                try {
                    messageListener.onMessage(message);
                    statistics.incrementMessagesReceived();
                    work.end(session, message);
                    return true;
                } catch (RuntimeException e) {
                    monitor.receiveError(listenerUri, e);
                    work.rollback(session);
                } catch (Error e) {
                    monitor.receiveError(listenerUri, e);
                    work.rollback(session);
                }
                return false;
            } else {
                idle = true;
                work.end(session, message);
                return false;
            }


        }

        private void closeSession() {
            synchronized (connectionManager) {
                if (isDurable() && session != null) {
                    try {
                        session.unsubscribe(subscriptionName);
                    } catch (JMSException e) {
                        monitor.listenerError(listenerUri.toString(), e);
                    }
                }
                JmsHelper.closeQuietly(session);
            }
            session = null;
        }

        private void closeResources(boolean force) {
            synchronized (connectionManager) {
                if (cacheLevel < CACHE_ADMINISTERED_OBJECTS || force) {
                    JmsHelper.closeQuietly(consumer);
                    JmsHelper.closeQuietly(session);
                    consumer = null;
                    session = null;
                }
                if (cacheLevel == CACHE_NONE || force) {
                    JmsHelper.closeQuietly(connection);
                    connection = null;
                }
            }
        }

        private void setRecoveryMarker() {
            synchronized (recoverySyncMonitor) {
                previousRecoveryMarker = recoveryMarker;
            }
        }

    }

}
