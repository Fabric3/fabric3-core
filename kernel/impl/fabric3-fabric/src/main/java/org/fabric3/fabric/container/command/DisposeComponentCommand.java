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
package org.fabric3.fabric.container.command;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Removes a registered component.
 */
public class DisposeComponentCommand extends AbstractComponentCommand {
    private static final long serialVersionUID = 1894510885498647133L;

    public DisposeComponentCommand(PhysicalComponentDefinition definition) {
        super(definition);
    }

    public BuildComponentCommand getCompensatingCommand() {
        return new BuildComponentCommand(definition);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DisposeComponentCommand that = (DisposeComponentCommand) o;

        return !(definition != null ? !definition.equals(that.definition) : that.definition != null);
    }

    public int hashCode() {
        return (definition != null ? definition.hashCode() : 0);
    }

}