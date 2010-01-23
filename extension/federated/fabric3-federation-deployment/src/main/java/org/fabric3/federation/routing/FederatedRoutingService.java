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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.DeploymentCommand;
import org.fabric3.federation.command.DeploymentResponse;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.domain.RoutingException;
import org.fabric3.spi.domain.RoutingMonitor;
import org.fabric3.spi.domain.RoutingService;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.DeploymentUnit;
import org.fabric3.spi.topology.DomainTopologyService;
import org.fabric3.spi.topology.MessageException;

/**
 * A routing service implementation that broadcasts a deployment to a zone.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FederatedRoutingService implements RoutingService {
    private RoutingMonitor monitor;
    private DomainTopologyService topologyService;
    private long timeout = 3000;
    private ClassLoaderRegistry classLoaderRegistry;

    public FederatedRoutingService(@Reference DomainTopologyService topologyService,
                                   @Reference ClassLoaderRegistry classLoaderRegistry,
                                   @Monitor RoutingMonitor monitor) {
        this.topologyService = topologyService;
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
    }

    // TODO FIXME add timeout property and check return for rollback
    @Property(required = false)
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void route(Deployment deployment) throws RoutingException {
        for (String zone : deployment.getZones()) {
            try {
                monitor.routeCommands(zone);
                DeploymentUnit deploymentUnit = deployment.getDeploymentUnit(zone);
                List<Command> extensionCommands = deploymentUnit.getExtensionCommands();
                byte[] serializedExtensionCommands = serialize((Serializable) extensionCommands);
                List<Command> commands = deploymentUnit.getCommands();
                byte[] serializedCommands = serialize((Serializable) commands);
                Command command = new DeploymentCommand(serializedExtensionCommands, serializedCommands);
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
                stream.writeObject(command);
                byte[] serialized = bas.toByteArray();
                List<byte[]> serializedResponses = topologyService.sendSynchronousMessageToZone(zone, serialized, timeout);
                List<DeploymentResponse> responses = new ArrayList<DeploymentResponse>(serializedResponses.size());
                for (byte[] serializedResponse : serializedResponses) {
                    DeploymentResponse response = deserialize(serializedResponse);
                    responses.add(response);
                }
                // TODO check responses
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

    @SuppressWarnings({"unchecked"})
    private DeploymentResponse deserialize(byte[] commands) throws RoutingException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            InputStream stream = new ByteArrayInputStream(commands);
            // Deserialize the command set. As command set classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            return (DeploymentResponse) ois.readObject();
        } catch (IOException e) {
            throw new RoutingException(e);
        } catch (ClassNotFoundException e) {
            throw new RoutingException(e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                // ignore;
            }
        }
    }
}
