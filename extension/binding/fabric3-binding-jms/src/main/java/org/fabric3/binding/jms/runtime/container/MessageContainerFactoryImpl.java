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
 */
package org.fabric3.binding.jms.runtime.container;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_CONNECTION;

/**
 *
 */
public class MessageContainerFactoryImpl implements MessageContainerFactory {
    private static final int DEFAULT_TRX_TIMEOUT = 30;

    private ExecutorService executorService;
    private TransactionManager tm;
    private MessageContainerMonitor containerMonitor;
    private HostInfo hostInfo;

    private int transactionTimeout = DEFAULT_TRX_TIMEOUT;   // in seconds per the JTA spec

    @Property(required = false)
    public void setTransactionTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid transaction timeout: " + timeout);
        }
        this.transactionTimeout = timeout;
    }

    public MessageContainerFactoryImpl(@Reference(name = "executorService") ExecutorService executorService,
                                       @Reference TransactionManager tm,
                                       @Reference HostInfo hostInfo,
                                       @Monitor MessageContainerMonitor containerMonitor) {
        this.executorService = executorService;
        this.tm = tm;
        this.hostInfo = hostInfo;
        this.containerMonitor = containerMonitor;
    }

    public AdaptiveMessageContainer create(ContainerConfiguration configuration) {
        ConnectionFactory factory = configuration.getFactory();
        SessionType type = configuration.getSessionType();
        boolean durable = configuration.isDurable();
        int cacheLevel = configuration.getCacheLevel();
        boolean cacheConnection = cacheLevel >= CACHE_CONNECTION;

        // set the receive timeout to half of the trx timeout - note receive timeout is in milliseconds and transaction timeout is in seconds
        int receiveTimeout = (transactionTimeout / 2) * 500;

        ContainerStatistics statistics = new ContainerStatistics();
        URI uri = configuration.getUri();
        ConnectionManager connectionManager = new ConnectionManager(factory, uri, cacheConnection, durable, containerMonitor);
        UnitOfWork work = createWork(uri, type, statistics);
        boolean javaEE = hostInfo.isJavaEEXAEnabled();
        return new AdaptiveMessageContainer(configuration, receiveTimeout, connectionManager, work, statistics, executorService, javaEE, containerMonitor);
    }

    private UnitOfWork createWork(URI uri, SessionType type, ContainerStatistics statistics) {
        switch (type) {
            case GLOBAL_TRANSACTED:
                return new JtaUnitOfWork(uri, transactionTimeout, tm, statistics);
            case LOCAL_TRANSACTED:
                return new LocalTransactionUnitOfWork(uri, statistics);
            case CLIENT_ACKNOWLEDGE:
                return new ClientAckUnitOfWork(uri);
            default:
                return new AutoAckUnitOfWork();
        }
    }
}
