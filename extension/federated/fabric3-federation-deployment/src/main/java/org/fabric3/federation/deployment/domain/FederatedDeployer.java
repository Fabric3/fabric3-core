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
package org.fabric3.federation.deployment.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.DeploymentResponse;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.RemoteSystemException;
import org.fabric3.spi.federation.Response;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.DeploymentUnit;

/**
 * A Deployer that consistently deploys the contents of a {@link DeploymentPackage} to a set of zones in a distributed domain.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FederatedDeployer implements Deployer {
    private DeployerMonitor monitor;
    private DomainTopologyService topologyService;
    private SerializationService serializationService;
    private long timeout = 3000;

    public FederatedDeployer(@Reference DomainTopologyService topologyService,
                             @Reference SerializationService serializationService,
                             @Monitor DeployerMonitor monitor) {
        this.topologyService = topologyService;
        this.serializationService = serializationService;
        this.monitor = monitor;
    }

    // TODO FIXME add timeout property and check return for rollback
    @Property(required = false)
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void deploy(DeploymentPackage deploymentPackage) throws DeploymentException {
        Deployment currentDeployment = deploymentPackage.getCurrentDeployment();
        Deployment fullDeployment = deploymentPackage.getFullDeployment();

        for (String zone : currentDeployment.getZones()) {
            try {
                monitor.deploy(zone);

                DeploymentCommand command = createCommand(zone, currentDeployment, fullDeployment);

                List<Response> responses = topologyService.sendSynchronousToZone(zone, command, timeout);
                boolean error = false;
                for (Response response : responses) {
                    String runtimeName = response.getRuntimeName();
                    if (response instanceof RemoteSystemException) {
                        error = true;
                        monitor.systemDeploymentError(runtimeName, ((RemoteSystemException) response).getThrowable());
                    } else if (response instanceof DeploymentResponse) {
                        DeploymentResponse deploymentResponse = (DeploymentResponse) response;
                        if (DeploymentResponse.FAILURE == deploymentResponse.getCode()) {
                            error = true;
                            monitor.deploymentError(runtimeName, deploymentResponse.getDeploymentException());
                        }
                    } else {
                        throw new DeploymentException("Unknown response type: " + response);
                    }
                }
                if (error) {
                    throw new DeploymentException("Deployment errors encountered and logged");
                }
            } catch (IOException e) {
                throw new DeploymentException(e);
            } catch (MessageException e) {
                throw new DeploymentException(e);
            }
        }
    }

    private DeploymentCommand createCommand(String zone, Deployment currentDeployment, Deployment fullDeployment) throws IOException {
        DeploymentUnit currentDeploymentUnit = currentDeployment.getDeploymentUnit(zone);
        SerializedDeploymentUnit currentSerializedUnit = createSerializedUnit(currentDeploymentUnit);
        DeploymentUnit fullDeploymentUnit = fullDeployment.getDeploymentUnit(zone);
        SerializedDeploymentUnit fullSerializedUnit = createSerializedUnit(fullDeploymentUnit);
        return new DeploymentCommand(currentSerializedUnit, fullSerializedUnit);
    }

    private SerializedDeploymentUnit createSerializedUnit(DeploymentUnit deploymentUnit) throws IOException {
        List<Command> extensionCommands = deploymentUnit.getExtensionCommands();
        byte[] serializedExtensionCommands = serializationService.serialize((Serializable) extensionCommands);
        List<Command> commands = deploymentUnit.getCommands();
        byte[] serializedCommands = serializationService.serialize((Serializable) commands);
        return new SerializedDeploymentUnit(serializedExtensionCommands, serializedCommands);
    }

}
