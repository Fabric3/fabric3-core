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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.federation.deployment.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.domain.generator.Deployment;
import org.fabric3.spi.domain.generator.DeploymentUnit;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.federation.topology.ControllerTopologyService;
import org.fabric3.spi.federation.topology.ErrorResponse;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.RuntimeInstance;
import org.fabric3.spi.federation.topology.Zone;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * A Deployer that deploys the contents of a {@link DeploymentPackage} to a set of zones in a distributed domain using commit/rollback semantics.
 */
@EagerInit
public class FederatedDeployer implements Deployer {
    private DeployerMonitor monitor;
    private ControllerTopologyService topologyService;
    private SerializationService serializationService;
    private long timeout = 60000;

    public FederatedDeployer(@Reference ControllerTopologyService topologyService,
                             @Reference SerializationService serializationService,
                             @Monitor DeployerMonitor monitor) {
        this.topologyService = topologyService;
        this.serializationService = serializationService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public synchronized void deploy(DeploymentPackage deploymentPackage) throws DeploymentException {
        Deployment currentDeployment = deploymentPackage.getCurrentDeployment();
        Deployment fullDeployment = deploymentPackage.getFullDeployment();

        // tracks deployment responses by zone
        for (String zoneName : currentDeployment.getZones()) {
            monitor.deploy(zoneName);
            DeploymentCommand command;
            try {
                command = createCommand(zoneName, currentDeployment, fullDeployment);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }

            Zone zone = new Zone(zoneName, Collections.<RuntimeInstance>emptyList());
            if (!topologyService.getZones().contains(zone)) {
                // no participants available, proceed with deployment but do not attempt to send to the zone
                monitor.participantNotAvailable(zoneName);
            } else {
                List<Response> responses;
                try {
                    // send deployments in a fail-fast manner
                    responses = topologyService.sendSynchronousToZone(zoneName, command, true, timeout);
                } catch (MessageException e) {
                    // rollback(zone, currentDeployment, completed, zoneResponses);
                    throw new DeploymentException(e);
                }

                if (responses.isEmpty()) {
                    throw new DeploymentException("Deployment responses not received");
                }
                Response last = responses.get(responses.size() - 1);

                String runtimeName = last.getRuntimeName();
                if (last instanceof ErrorResponse) {
                    ErrorResponse response = (ErrorResponse) last;
                    monitor.deploymentError(runtimeName, response.getException());
                    // rollback(zone, currentDeployment, completed, zoneResponses);
                    throw new DeploymentException("Deployment errors encountered and logged");
                }
            }
        }
    }

    private DeploymentCommand createCommand(String zone, Deployment currentDeployment, Deployment fullDeployment) throws IOException {
        DeploymentUnit currentDeploymentUnit = currentDeployment.getDeploymentUnit(zone);
        SerializedDeploymentUnit currentSerializedUnit = createSerializedUnit(currentDeploymentUnit);
        DeploymentUnit fullDeploymentUnit = fullDeployment.getDeploymentUnit(zone);
        SerializedDeploymentUnit fullSerializedUnit = createSerializedUnit(fullDeploymentUnit);
        return new DeploymentCommand(zone, currentSerializedUnit, fullSerializedUnit);
    }

    private SerializedDeploymentUnit createSerializedUnit(DeploymentUnit deploymentUnit) throws IOException {
        List<CompensatableCommand> provisionCommands = deploymentUnit.getProvisionCommands();
        byte[] serializedProvisionCommands = serializationService.serialize((Serializable) provisionCommands);

        List<CompensatableCommand> extensionCommands = deploymentUnit.getExtensionCommands();
        byte[] serializedExtensionCommands = serializationService.serialize((Serializable) extensionCommands);
        List<CompensatableCommand> commands = deploymentUnit.getCommands();
        byte[] serializedCommands = serializationService.serialize((Serializable) commands);
        return new SerializedDeploymentUnit(serializedProvisionCommands, serializedExtensionCommands, serializedCommands);
    }
}
