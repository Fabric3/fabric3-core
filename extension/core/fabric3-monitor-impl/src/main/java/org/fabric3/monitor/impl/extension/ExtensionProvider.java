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
package org.fabric3.monitor.impl.extension;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.monitor.impl.common.MonitorConstants;
import org.fabric3.monitor.impl.destination.DefaultMonitorDestination;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderFactory;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.writer.EventWriter;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.xml.LocationAwareXMLStreamReader;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * Instantiates and (un)registers default monitor destinations with the destination registry.
 */
@EagerInit
public class ExtensionProvider {
    private MonitorDestinationRegistry registry;
    private EventWriter eventWriter;
    private AppenderFactory appenderFactory;
    private ExtensionProviderMonitor monitor;

    private int capacity = MonitorConstants.DEFAULT_BUFFER_CAPACITY;
    private boolean overrideDefault;

    private LocationAwareXMLStreamReader systemReader;

    @Property(required = false)
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Property(required = false)
    public void setOverrideDefault(boolean overrideDefault) {
        this.overrideDefault = overrideDefault;
    }

    @Property(required = false)
    public void setDefaultAppenders(XMLStreamReader reader) {
        systemReader = new LocationAwareXMLStreamReader(reader, "system configuration");
    }

    public ExtensionProvider(@Reference MonitorDestinationRegistry registry,
                             @Reference EventWriter eventWriter,
                             @Reference AppenderFactory appenderFactory,
                             @Reference RingBufferDestinationRouter router,
                             @Reference EventService eventService,
                             @Monitor ExtensionProviderMonitor monitor) {
        this.registry = registry;
        this.eventWriter = eventWriter;
        this.appenderFactory = appenderFactory;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        try {
            if (overrideDefault) {
                // default destination already registered
                return;
            }
            List<Appender> defaultAppenders;
            if (systemReader == null) {
                defaultAppenders = appenderFactory.instantiateDefaultAppenders();
            } else {
                defaultAppenders = appenderFactory.instantiate(systemReader);
            }
            // register the default destination as index 0
            MonitorDestination defaultDestination = new DefaultMonitorDestination(DEFAULT_DESTINATION, eventWriter, capacity, defaultAppenders);
            defaultDestination.start();
            registry.register(defaultDestination);
            systemReader = null;
        } catch (ContainerException | XMLStreamException | IOException e) {
            monitor.error(e);
        }
    }

    public interface ExtensionProviderMonitor {

        @Severe
        void error(Throwable e);

    }
}
