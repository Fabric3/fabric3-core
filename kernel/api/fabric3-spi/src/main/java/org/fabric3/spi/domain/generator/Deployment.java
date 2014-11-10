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
package org.fabric3.spi.domain.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.host.Names;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.CompensatableCommand;

/**
 * Used to deploy composites to the domain. Deployments contain 1..N {@link DeploymentUnit}s which are provisioned to zones in the domain.
 * <p/>
 * A DeploymentUnit is composed of 1..N {@link Command}s, which are executed on a runtime to perform tasks such as creating a component or wire.
 */
public class Deployment {
    private String id;

    private Map<String, DeploymentUnit> units = new HashMap<>();

    public Deployment(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void addProvisionCommand(String zone, CompensatableCommand command) {
        DeploymentUnit unit = getDeploymentUnit(zone);
        unit.addProvisionCommand(command);
    }

    public void addProvisionCommands(String zone, List<CompensatableCommand> commands) {
        DeploymentUnit unit = getDeploymentUnit(zone);
        unit.addProvisionCommands(commands);
    }

    public void addExtensionCommand(String zone, CompensatableCommand command) {
        DeploymentUnit unit = getDeploymentUnit(zone);
        unit.addExtensionCommand(command);
    }

    public void addExtensionCommands(String zone, List<CompensatableCommand> commands) {
        DeploymentUnit unit = getDeploymentUnit(zone);
        unit.addExtensionCommands(commands);
    }

    public void addCommand(String zone, CompensatableCommand command) {
        DeploymentUnit unit = getDeploymentUnit(zone);
        unit.addCommand(command);
    }

    public void addCommands(String zone, List<CompensatableCommand> commands) {
        DeploymentUnit unit = getDeploymentUnit(zone);
        unit.addCommands(commands);
    }

    public Set<String> getZones() {
        return units.keySet();
    }

    public DeploymentUnit getDeploymentUnit() {
        if (units.isEmpty()) {
            DeploymentUnit unit = new DeploymentUnit();
            units.put(Names.LOCAL_ZONE, unit);
            return unit;
        }
        return units.values().iterator().next();
    }

    public DeploymentUnit getDeploymentUnit(String zone) {
        DeploymentUnit unit = units.get(zone);
        if (unit == null) {
            unit = new DeploymentUnit();
            units.put(zone, unit);
        }
        return unit;
    }

    public Map<String, List<CompensatableCommand>> getCommands() {
        Map<String, List<CompensatableCommand>> ret = new HashMap<>();
        for (Map.Entry<String, DeploymentUnit> entry : units.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().getCommands());
        }
        return ret;
    }

}
