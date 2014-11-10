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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.domain.generator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.container.command.CompensatableCommand;

/**
 * Used to deploy composites to a zone. Provision commands are executed first, followed by extension commands as deployment commands may require
 * artifacts or extension capabilities.
 */
public class DeploymentUnit implements Serializable {
    private static final long serialVersionUID = -5868891769973094096L;

    private List<CompensatableCommand> provisionCommands = new ArrayList<>();
    private List<CompensatableCommand> extensionCommands = new ArrayList<>();
    private List<CompensatableCommand> commands = new ArrayList<>();

    public void addProvisionCommand(CompensatableCommand command) {
        provisionCommands.add(command);
    }

    public void addProvisionCommands(List<CompensatableCommand> command) {
        provisionCommands.addAll(command);
    }

    public List<CompensatableCommand> getProvisionCommands() {
        return provisionCommands;
    }

    public void addExtensionCommand(CompensatableCommand command) {
        extensionCommands.add(command);
    }

    public void addExtensionCommands(List<CompensatableCommand> command) {
        extensionCommands.addAll(command);
    }

    public List<CompensatableCommand> getExtensionCommands() {
        return extensionCommands;
    }

    public void addCommand(CompensatableCommand command) {
        commands.add(command);
    }

    public void addCommands(List<CompensatableCommand> command) {
        commands.addAll(command);
    }

    public List<CompensatableCommand> getCommands() {
        return commands;
    }
}
