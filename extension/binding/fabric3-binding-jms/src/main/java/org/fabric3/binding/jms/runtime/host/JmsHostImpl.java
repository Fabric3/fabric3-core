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

package org.fabric3.binding.jms.runtime.host;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.jms.runtime.container.AdaptiveMessageContainer;
import org.fabric3.binding.jms.runtime.container.MessageContainerMonitor;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.transport.Transport;

/**
 * JmsHost implementation that registers JMS MessageListeners with an AdaptiveMessageContainer to receive messages and dispatch them to a service
 * endpoint.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces = {JmsHost.class, Transport.class})
public class JmsHostImpl implements JmsHost, Transport, Fabric3EventListener<RuntimeStart> {
    private Map<URI, AdaptiveMessageContainer> containers = new ConcurrentHashMap<URI, AdaptiveMessageContainer>();
    private boolean started;
    private EventService eventService;
    private ExecutorService executorService;
    private TransactionManager tm;
    private MessageContainerMonitor containerMonitor;
    private ManagementService managementService;
    private HostMonitor monitor;

    public JmsHostImpl(@Reference EventService eventService,
                       @Reference ExecutorService executorService,
                       @Reference TransactionManager tm,
                       @Reference ManagementService managementService,
                       @Monitor MessageContainerMonitor containerMonitor,
                       @Monitor HostMonitor monitor) {
        this.eventService = eventService;
        this.executorService = executorService;
        this.tm = tm;
        this.managementService = managementService;
        this.containerMonitor = containerMonitor;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStart.class, this);
    }

    @Destroy
    public void destroy() throws JMSException {
        for (AdaptiveMessageContainer container : containers.values()) {
            container.stop();
        }
        for (AdaptiveMessageContainer container : containers.values()) {
            container.shutdown();
        }
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
                monitor.error("Error stopping service listener: " + entry.getKey(), e);
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
                entry.getValue().start();
            } catch (JMSException e) {
                monitor.error("Error starting service listener: " + entry.getKey(), e);
            }
        }
        started = false;
        started = true;
    }

    public void onEvent(RuntimeStart event) {
        // start receiving messages after the runtime has started
        for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
            try {
                entry.getValue().initialize();
                monitor.registerListener(entry.getKey());
            } catch (JMSException e) {
                monitor.error("Error starting service listener: " + entry.getKey(), e);
            }
        }
        started = true;
    }

    public boolean isRegistered(URI serviceUri) {
        return containers.containsKey(serviceUri);
    }

    public void register(ListenerConfiguration configuration) throws JMSException {
        Destination destination = configuration.getDestination();
        MessageListener listener = configuration.getMessageListener();
        ConnectionFactory factory = configuration.getFactory();
        TransactionType type = configuration.getType();
        URI serviceUri = configuration.getUri();
        AdaptiveMessageContainer container = new AdaptiveMessageContainer(destination, listener, factory, executorService, tm, containerMonitor);
        if (TransactionType.GLOBAL == type) {
            container.setTransactionTypeProperty(TransactionType.GLOBAL);
            container.setAcknowledgeModeProperty(Session.AUTO_ACKNOWLEDGE);
        }
        container.setCacheLevel(configuration.getCacheLevel());
        container.setExceptionListener(configuration.getExceptionListener());
        container.setMaxMessagesToProcess(configuration.getMaxMessagesToProcess());
        container.setCacheLevel(configuration.getCacheLevel());
        container.setMaxReceivers(configuration.getMaxReceivers());
        container.setMinReceivers(configuration.getMinReceivers());
        container.setRecoveryInterval(configuration.getRecoveryInterval());
        container.setIdleLimit(configuration.getIdleLimit());
        container.setReceiveTimeout(configuration.getReceiveTimeout());
        container.setRecoveryInterval(configuration.getRecoveryInterval());
// TODO additional configuration        
//        container.setClientId();
//        container.setDurable();
//        container.setDurableSubscriptionName();
//        container.setLocalDelivery();
        containers.put(serviceUri, container);

        try {
            String encoded = encode(serviceUri);
            managementService.export(serviceUri.getFragment(), encoded, "JMS message container", container);
        } catch (ManagementException e) {
            throw new JMSException(e.getMessage());
        }
        if (started) {
            container.initialize();
            monitor.registerListener(serviceUri);
        }
    }

    public void unregister(URI serviceUri) throws JMSException {
        AdaptiveMessageContainer container = containers.remove(serviceUri);
        if (container != null) {
            container.shutdown();
            try {
                String encoded = encode(serviceUri);
                managementService.export(serviceUri.getFragment(), encoded, "JMS message container", container);
            } catch (ManagementException e) {
                throw new JMSException(e.getMessage());
            }
            monitor.unRegisterListener(serviceUri);
        }
    }

    private String encode(URI serviceUri) {
        return "JMS message containers/" + serviceUri.getPath().substring(1);
    }


}
