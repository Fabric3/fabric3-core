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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.event.RuntimeStop;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.transport.Transport;
import org.fabric3.spi.util.UriHelper;

import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_CONNECTION;

/**
 *
 */
@EagerInit
@Service(names = {MessageContainerManager.class, Transport.class})
public class MessageContainerManagerImpl implements MessageContainerManager, Transport {
    private static final int DEFAULT_TRX_TIMEOUT = 30;
    private Map<URI, AdaptiveMessageContainer> containers = new ConcurrentHashMap<URI, AdaptiveMessageContainer>();
    private boolean started;
    private boolean pausedOnStart;
    private EventService eventService;
    private ExecutorService executorService;
    private TransactionManager tm;
    private MessageContainerMonitor containerMonitor;
    private ManagementService managementService;
    private HostInfo hostInfo;

    private ContainerManagerMonitor managerMonitor;
    private int transactionTimeout = DEFAULT_TRX_TIMEOUT;

    public MessageContainerManagerImpl(@Reference EventService eventService,
                                       @Reference ExecutorService executorService,
                                       @Reference TransactionManager tm,
                                       @Reference ManagementService managementService,
                                       @Reference HostInfo hostInfo,
                                       @Monitor MessageContainerMonitor containerMonitor,
                                       @Monitor ContainerManagerMonitor managerMonitor) {
        this.eventService = eventService;
        this.executorService = executorService;
        this.tm = tm;
        this.managementService = managementService;
        this.hostInfo = hostInfo;
        this.containerMonitor = containerMonitor;
        this.managerMonitor = managerMonitor;
    }

    @Property(required = false)
    public void setTransactionTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid transaction timeout: " + timeout);
        }
        this.transactionTimeout = timeout;
    }

    @Property(required = false)
    public void setPauseOnStart(boolean pauseOnStart) {
        this.pausedOnStart = pauseOnStart;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStart.class, new StartEventListener());
        eventService.subscribe(RuntimeStop.class, new StopEventListener());
    }

    @Destroy
    public void destroy() throws JMSException {
        started = false;
    }

    public void suspend() {
        if (!started) {
            return;
        }
        for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
            try {
                entry.getValue().stop();
            } catch (JMSException e) {
                managerMonitor.stopError(entry.getKey(), e);
            }
        }
        started = false;
    }

    public void resume() {
        if (started) {
            return;
        }
        for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
            try {
                if (pausedOnStart) {
                    entry.getValue().initialize();
                } else {
                    entry.getValue().start();
                }
            } catch (JMSException e) {
                managerMonitor.startError(entry.getKey(), e);
            }
        }
        pausedOnStart = false;
        started = true;
    }

    public boolean isRegistered(URI serviceUri) {
        return containers.containsKey(serviceUri);
    }

    public void register(ContainerConfiguration configuration) throws JMSException {
        URI uri = configuration.getUri();
        if (containers.containsKey(uri)) {
            throw new JMSException("Container already registered: " + uri);
        }
        ConnectionFactory factory = configuration.getFactory();
        TransactionType type = configuration.getType();
        String clientId = configuration.getClientId();
        boolean durable = configuration.isDurable();
        int cacheLevel = configuration.getCacheLevel();
        boolean cacheConnection = cacheLevel >= CACHE_CONNECTION;

        // set the receive timeout to half of the trx timeout
        int receiveTimeout = transactionTimeout / 2;

        ContainerStatistics statistics = new ContainerStatistics();
        ConnectionManager connectionManager = new ConnectionManager(factory, uri, clientId, cacheConnection, durable, containerMonitor);
        UnitOfWork transactionHelper = new UnitOfWork(uri, type, transactionTimeout, tm, statistics);
        AdaptiveMessageContainer container = new AdaptiveMessageContainer(configuration,
                                                                          receiveTimeout,
                                                                          connectionManager,
                                                                          transactionHelper,
                                                                          statistics,
                                                                          executorService,
                                                                          hostInfo.isJavaEEXAEnabled(),
                                                                          containerMonitor);
        containers.put(uri, container);

        try {
            String encodedName = encodeName(uri);
            String encodedGroup = encodeGroup(uri);
            managementService.export(encodedName, encodedGroup, "JMS message container", container);
        } catch (ManagementException e) {
            throw new JMSException(e.getMessage());
        }
        if (started) {
            container.initialize();
            managerMonitor.registerListener(uri);
        }
    }

    public void unregister(URI uri) throws JMSException {
        AdaptiveMessageContainer container = containers.remove(uri);
        if (container != null) {
            container.shutdown();
            try {
                String encodedName = encodeName(uri);
                String encodedGroup = encodeGroup(uri);
                managementService.remove(encodedName, encodedGroup);
            } catch (ManagementException e) {
                throw new JMSException(e.getMessage());
            }
            managerMonitor.unRegisterListener(uri);
        }
    }

    private String encodeName(URI uri) {
        return "transports/jms/consumers/" + UriHelper.getBaseName(uri).replace("#", "/").toLowerCase();
    }

    private String encodeGroup(URI uri) {
        String path = uri.getPath();
        if (path.length() != 0) {
            return "JMS/message containers/" + path.substring(1);
        }
        return "JMS/message containers/" + uri.getAuthority();
    }

    private class StartEventListener implements Fabric3EventListener<RuntimeStart> {

        public void onEvent(RuntimeStart event) {
            // start receiving messages after the runtime has started
            for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
                try {
                    if (!pausedOnStart) {
                        entry.getValue().initialize();
                    }
                    managerMonitor.registerListener(entry.getKey());
                } catch (JMSException e) {
                    managerMonitor.startError(entry.getKey(), e);
                }
            }
            if (!pausedOnStart) {
                started = true;
            }

        }
    }

    private class StopEventListener implements Fabric3EventListener<RuntimeStop> {

        public void onEvent(RuntimeStop event) {
            for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
                try {
                    entry.getValue().stop();
                } catch (JMSException e) {
                    managerMonitor.stopError(entry.getKey(), e);
                }
            }
            for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
                try {
                    entry.getValue().shutdown();
                } catch (JMSException e) {
                    managerMonitor.stopError(entry.getKey(), e);
                }
            }

        }
    }

}
