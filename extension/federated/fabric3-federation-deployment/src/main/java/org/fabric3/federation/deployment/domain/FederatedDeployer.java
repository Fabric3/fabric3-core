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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.deployment.command.CommitCommand;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RollbackCommand;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.federation.deployment.spi.FederatedDeployerListener;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.ErrorResponse;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.Response;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.DeploymentUnit;

/**
 * A Deployer that deploys the contents of a {@link DeploymentPackage} to a set of zones in a distributed domain using commit/rollback semantics.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FederatedDeployer implements Deployer {
    private DeployerMonitor monitor;
    private DomainTopologyService topologyService;
    private SerializationService serializationService;
    private List<FederatedDeployerListener> listeners;
    private long timeout = 3000;

    public FederatedDeployer(@Reference DomainTopologyService topologyService,
                             @Reference SerializationService serializationService,
                             @Monitor DeployerMonitor monitor) {
        this.topologyService = topologyService;
        this.serializationService = serializationService;
        this.monitor = monitor;
        this.listeners = new ArrayList<FederatedDeployerListener>();
    }

    @Reference(required = false)
    public void setListeners(List<FederatedDeployerListener> listeners) {
        this.listeners = listeners;
    }

    @Property(required = false)
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public void deploy(DeploymentPackage deploymentPackage) throws DeploymentException {
        Deployment currentDeployment = deploymentPackage.getCurrentDeployment();
        Deployment fullDeployment = deploymentPackage.getFullDeployment();

        // tracks deployment responses by zone
        Map<String, List<Response>> zoneResponses = new HashMap<String, List<Response>>();
        List<DeploymentCommand> completed = new ArrayList<DeploymentCommand>();
        for (String zone : currentDeployment.getZones()) {
            monitor.deploy(zone);
            DeploymentCommand command;
            try {
                command = createCommand(zone, currentDeployment, fullDeployment);
            } catch (IOException e) {
                // rollback previous zones
                rollback(zone, currentDeployment, completed, zoneResponses);
                throw new DeploymentException(e);
            }
            notifyDeploy(command);
            List<Response> responses;
            try {
                // send deployments in a fail-fast manner
                responses = topologyService.sendSynchronousToZone(zone, command, true, timeout);
            } catch (MessageException e) {
                rollback(zone, currentDeployment, completed, zoneResponses);
                throw new DeploymentException(e);
            }

            if (responses.isEmpty()) {
                throw new DeploymentException("Deployment responses not received");
            }
            completed.add(command);
            zoneResponses.put(zone, responses);
            Response last = responses.get(responses.size() - 1);

            String runtimeName = last.getRuntimeName();
            if (last instanceof ErrorResponse) {
                ErrorResponse response = (ErrorResponse) last;
                monitor.deploymentError(runtimeName, response.getException());
                rollback(zone, currentDeployment, completed, zoneResponses);
                throw new DeploymentException("Deployment errors encountered and logged");
            }
        }
        for (DeploymentCommand command : completed) {
            // send the commit
            try {
                String zone = command.getZone();
                monitor.commit(zone);
                CommitCommand commitCommand = new CommitCommand();
                topologyService.sendSynchronousToZone(zone, commitCommand, false, timeout);
            } catch (MessageException e) {
                // TODO handle heuristic rollback - runtimes should rollback after a wait period
                throw new DeploymentException(e);
            }
            notifyCompletion(command);
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
        List<CompensatableCommand> extensionCommands = deploymentUnit.getExtensionCommands();
        byte[] serializedExtensionCommands = serializationService.serialize((Serializable) extensionCommands);
        List<CompensatableCommand> commands = deploymentUnit.getCommands();
        byte[] serializedCommands = serializationService.serialize((Serializable) commands);
        return new SerializedDeploymentUnit(serializedExtensionCommands, serializedCommands);
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    private void rollback(String failedZone, Deployment deployment, List<DeploymentCommand> completed, Map<String, List<Response>> zoneResponses) {
        for (Map.Entry<String, List<Response>> entry : zoneResponses.entrySet()) {
            List<Response> responses = entry.getValue();
            String zone = entry.getKey();
            monitor.rollback(zone);
            for (Response response : responses) {
                String runtimeName = response.getRuntimeName();
                try {
                    DeploymentCommand rollback = createRollbackCommand(zone, deployment);
                    if (failedZone.equals(zone) && response == responses.get(responses.size() - 1)) {
                        // The last runtime in the failed zone is where the error occurred and it will have rolled back the deployment.
                        // Return as the remaining zones never received a deployment due to this fail-fast behavior.
                        return;
                    }
                    // send the rollback deployment command
                    Response rollbackResponse = topologyService.sendSynchronous(runtimeName, rollback, timeout);
                    if (rollbackResponse instanceof ErrorResponse) {
                        ErrorResponse errorResponse = (ErrorResponse) rollbackResponse;
                        monitor.deploymentError(runtimeName, errorResponse.getException());
                    }
                    // send the rollback notification
                    RollbackCommand rollbackCommand = new RollbackCommand();
                    topologyService.sendSynchronous(runtimeName, rollbackCommand, timeout);
                } catch (MessageException e) {
                    monitor.rollbackError(runtimeName, e);
                } catch (IOException e) {
                    monitor.rollbackError(runtimeName, e);
                }
            }
        }
        notifyRollback(completed);

    }

    private DeploymentCommand createRollbackCommand(String zone, Deployment deployment) throws IOException {
        DeploymentUnit unit = deployment.getDeploymentUnit(zone);
        DeploymentUnit compensatingUnit = new DeploymentUnit();
        if (!unit.getExtensionCommands().isEmpty()) {
            ListIterator<CompensatableCommand> iter = unit.getExtensionCommands().listIterator(unit.getExtensionCommands().size() - 1);
            while (iter.hasPrevious()) {
                CompensatableCommand command = iter.previous();
                CompensatableCommand compensating = command.getCompensatingCommand();
                compensatingUnit.addExtensionCommand(compensating);
            }
        }
        if (!unit.getCommands().isEmpty()) {
            ListIterator<CompensatableCommand> iter = unit.getCommands().listIterator(unit.getCommands().size() - 1);
            while (iter.hasPrevious()) {
                CompensatableCommand command = iter.previous();
                CompensatableCommand compensating = command.getCompensatingCommand();
                compensatingUnit.addCommand(compensating);
            }
        }
        SerializedDeploymentUnit serializedUnit = createSerializedUnit(compensatingUnit);
        return new DeploymentCommand(zone, serializedUnit, serializedUnit);
    }

    private void notifyRollback(List<DeploymentCommand> completed) {
        for (DeploymentCommand command : completed) {
            for (FederatedDeployerListener listener : listeners) {
                listener.onRollback(command);
            }
        }
    }

    private void notifyDeploy(DeploymentCommand command) throws DeploymentException {
        for (FederatedDeployerListener listener : listeners) {
            listener.onDeploy(command);
        }
    }

    private void notifyCompletion(DeploymentCommand command) {
        for (FederatedDeployerListener listener : listeners) {
            listener.onCompletion(command);
        }
    }


}
