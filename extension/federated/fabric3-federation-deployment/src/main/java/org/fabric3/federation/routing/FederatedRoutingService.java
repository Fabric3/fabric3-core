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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.federation.routing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.ZoneDeploymentCommand;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.domain.RoutingException;
import org.fabric3.spi.domain.RoutingMonitor;
import org.fabric3.spi.domain.RoutingService;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.topology.DomainManager;
import org.fabric3.spi.topology.MessageException;

/**
 * A routing service implementation that routes commands to a zone.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FederatedRoutingService implements RoutingService {
    private final DomainManager domainManager;
    private final RoutingMonitor monitor;

    public FederatedRoutingService(@Reference DomainManager domainManager, @Monitor RoutingMonitor monitor) {
        this.domainManager = domainManager;
        this.monitor = monitor;
    }

    public void route(CommandMap commandMap) throws RoutingException {
        String id = commandMap.getId();
        for (String zone : commandMap.getZones()) {
            try {
                monitor.routeCommands(zone);
                List<Command> extensionCommands = commandMap.getZoneCommands(zone).getExtensionCommands();
                byte[] serializedExtensionCommands = serialize((Serializable) extensionCommands);
                List<Command> commands = commandMap.getZoneCommands(zone).getCommands();
                byte[] serializedCommands = serialize((Serializable) commands);
                String correlationId = commandMap.getCorrelationId();
                boolean synchronization = commandMap.isSynchornization();
                Command command = new ZoneDeploymentCommand(id, serializedExtensionCommands, serializedCommands, correlationId, synchronization);
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
                stream.writeObject(command);
                domainManager.sendMessage(zone, bas.toByteArray());
            } catch (IOException e) {
                throw new RoutingException(e);
            } catch (MessageException e) {
                throw new RoutingException(e);
            }
        }

    }

    private byte[] serialize(Serializable serializable) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
        stream.writeObject(serializable);
        return bas.toByteArray();
    }
}
