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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;

/**
 * Used to deploy composites to the domain. Deployments contain 1..N {@link DeploymentUnit}s which are provisioned to zones in the domain. A
 * DeploymentUnit is composed of 1..N {@link Command}s, which are executed on a participant runtime to perform tasks such as creating a component or
 * wire.
 */
public class Deployment {
    private String id;

    private Map<String, DeploymentUnit> units = new HashMap<String, DeploymentUnit>();

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

    public DeploymentUnit getDeploymentUnit(String zone) {
        DeploymentUnit unit = units.get(zone);
        if (unit == null) {
            unit = new DeploymentUnit();
            units.put(zone, unit);
        }
        return unit;
    }

    public Map<String, List<CompensatableCommand>> getCommands() {
        Map<String, List<CompensatableCommand>> ret = new HashMap<String, List<CompensatableCommand>>();
        for (Map.Entry<String, DeploymentUnit> entry : units.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().getCommands());
        }
        return ret;
    }

}
