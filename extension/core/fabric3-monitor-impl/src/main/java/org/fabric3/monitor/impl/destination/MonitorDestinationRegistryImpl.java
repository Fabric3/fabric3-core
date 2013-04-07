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
package org.fabric3.monitor.impl.destination;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.impl.appender.factory.AppenderCreationException;
import org.fabric3.monitor.impl.appender.factory.AppenderFactory;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.host.monitor.DestinationRouter.DEFAULT_DESTINATION;

/**
 *
 */
@EagerInit
public class MonitorDestinationRegistryImpl implements MonitorDestinationRegistry {
    private AppenderFactory appenderFactory;
    private List<Appender> defaultAppenders = Collections.emptyList();

    private MonitorDestination[] destinations;

    public MonitorDestinationRegistryImpl(@Reference AppenderFactory appenderFactory) {
        this.appenderFactory = appenderFactory;
    }

    @Property(required = false)
    public void setDefaultAppenders(XMLStreamReader reader) throws AppenderCreationException, XMLStreamException {
        defaultAppenders = appenderFactory.instantiate(reader);
    }

    @Init
    public void init() throws IOException, AppenderCreationException {
        if (defaultAppenders.isEmpty()) {
            defaultAppenders = appenderFactory.instantiateDefaultAppenders();
        }

        destinations = new MonitorDestination[1];
        // register the default destination as index 0
        destinations[0] = new MonitorDestinationImpl(DEFAULT_DESTINATION, defaultAppenders);

        for (MonitorDestination destination : destinations) {
            destination.start();
        }
    }

    @Destroy
    public void destroy() throws IOException {
        for (MonitorDestination destination : destinations) {
            destination.stop();
        }
    }

    public void register(MonitorDestination destination) {
        throw new UnsupportedOperationException();
    }

    public MonitorDestination unregister(String name) {
        throw new UnsupportedOperationException();
    }

    public int getIndex(String name) {
        for (int i = 0; i < destinations.length; i++) {
            MonitorDestination destination = destinations[i];
            if (destination.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void write(int index, ByteBuffer buffer) throws IOException {
        if (index < 0 || index >= destinations.length) {
            throw new AssertionError("Invalid index: " + index);
        }
        destinations[index].write(buffer);
    }

}
