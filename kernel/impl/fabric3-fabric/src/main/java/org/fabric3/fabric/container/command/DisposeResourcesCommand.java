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

import java.util.List;

import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.model.physical.PhysicalResource;

/**
 * Removes resources on a runtime.
 */
public class DisposeResourcesCommand implements Command {
    private List<PhysicalResource> physicalResources;

    public DisposeResourcesCommand(List<PhysicalResource> physicalResources) {
        this.physicalResources = physicalResources;
    }

    public List<PhysicalResource> getPhysicalResources() {
        return physicalResources;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DisposeResourcesCommand that = (DisposeResourcesCommand) o;

        return !(physicalResources != null ? !physicalResources.equals(that.physicalResources) : that.physicalResources != null);
    }

    public int hashCode() {
        return (physicalResources != null ? physicalResources.hashCode() : 0);
    }

}