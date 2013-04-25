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
package org.fabric3.monitor.impl.extension;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.monitor.impl.destination.DefaultMonitorDestination;
import org.fabric3.monitor.impl.router.RingBufferDestinationRouter;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderCreationException;
import org.fabric3.monitor.spi.appender.AppenderFactory;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.writer.EventWriter;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.xml.LocationAwareXMLStreamReader;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 * Instantiates and (un)registers default monitor destinations with the destination registry.
 */
@EagerInit
public class ExtensionProvider {
    private MonitorDestinationRegistry registry;
    private EventWriter eventWriter;
    private AppenderFactory appenderFactory;
    private ExtensionProviderMonitor monitor;

    private int capacity = 3072;
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
    public void setDefaultAppenders(XMLStreamReader reader) throws AppenderCreationException, XMLStreamException {
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
    public void init() throws AppenderCreationException, IOException {
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
        } catch (AppenderCreationException e) {
            monitor.error(e);
        } catch (IOException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
            monitor.error(e);
        }
    }

    public interface ExtensionProviderMonitor {

        @Severe
        void error(Throwable e);

    }
}
