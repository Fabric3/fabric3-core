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

import javax.jms.JMSException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStart;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.fabric3.spi.runtime.event.TransportStart;
import org.fabric3.spi.runtime.event.TransportStop;
import org.fabric3.spi.transport.Transport;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 *
 */
@EagerInit
@Service({MessageContainerManager.class, Transport.class})
public class MessageContainerManagerImpl implements MessageContainerManager, Transport {
    private Map<URI, AdaptiveMessageContainer> containers = new ConcurrentHashMap<>();
    private boolean started;
    private boolean pausedOnStart;
    private EventService eventService;
    private ManagementService managementService;

    private ContainerManagerMonitor managerMonitor;

    public MessageContainerManagerImpl(@Reference EventService eventService,
                                       @Reference ManagementService managementService,
                                       @Monitor ContainerManagerMonitor managerMonitor) {
        this.eventService = eventService;
        this.managementService = managementService;
        this.managerMonitor = managerMonitor;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:jms/@pause.on.start")
    public void setPauseOnStart(boolean pauseOnStart) {
        this.pausedOnStart = pauseOnStart;
    }

    @Init
    public void init() {
        eventService.subscribe(TransportStart.class, new StartEventListener());
        eventService.subscribe(TransportStop.class, new StopEventListener());
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
            entry.getValue().stop();
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
            } catch (Fabric3Exception e) {
                managerMonitor.startError(entry.getKey(), e);
            }
        }
        pausedOnStart = false;
        started = true;
    }

    public boolean isRegistered(URI serviceUri) {
        return containers.containsKey(serviceUri);
    }

    public void register(AdaptiveMessageContainer container) throws Fabric3Exception {
        URI uri = container.getContainerUri();
        containers.put(uri, container);

        String encodedName = encodeName(uri);
        String encodedGroup = encodeGroup(uri);
        managementService.export(encodedName, encodedGroup, "JMS message container", container);
        if (started) {
            container.initialize();
            managerMonitor.registerListener(uri);
        }
    }

    public void unregister(URI uri) throws Fabric3Exception {
        AdaptiveMessageContainer container = containers.remove(uri);
        if (container != null) {
            container.shutdown();
            String encodedName = encodeName(uri);
            String encodedGroup = encodeGroup(uri);
            managementService.remove(encodedName, encodedGroup);
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

    private class StartEventListener implements Fabric3EventListener<TransportStart> {

        public void onEvent(TransportStart event) {
            // start receiving messages after the runtime has started
            for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
                try {
                    if (!pausedOnStart) {
                        entry.getValue().initialize();
                    }
                    managerMonitor.registerListener(entry.getKey());
                } catch (Fabric3Exception e) {
                    managerMonitor.startError(entry.getKey(), e);
                }
            }
            if (!pausedOnStart) {
                started = true;
            }

        }
    }

    private class StopEventListener implements Fabric3EventListener<TransportStop> {

        public void onEvent(TransportStop event) {
            for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
                entry.getValue().stop();
            }
            for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
                entry.getValue().shutdown();
            }

        }
    }

}
