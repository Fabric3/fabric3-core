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
package org.fabric3.binding.jms.runtime.container;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.api.host.runtime.HostInfo;
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
