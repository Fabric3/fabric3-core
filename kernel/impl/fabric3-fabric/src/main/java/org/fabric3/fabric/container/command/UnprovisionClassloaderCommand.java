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
package org.fabric3.fabric.container.command;

import java.net.URI;

import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

public class UnprovisionClassloaderCommand implements CompensatableCommand {
    private static final long serialVersionUID = -155817487398296922L;

    private PhysicalClassLoaderDefinition definition;

    public UnprovisionClassloaderCommand(PhysicalClassLoaderDefinition definition) {
        this.definition = definition;
    }

    public ProvisionClassloaderCommand getCompensatingCommand() {
        return new ProvisionClassloaderCommand(definition);
    }

    public PhysicalClassLoaderDefinition getClassLoaderDefinition() {
        return definition;
    }

    public URI getUri() {
        return definition.getUri();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnprovisionClassloaderCommand that = (UnprovisionClassloaderCommand) o;

        return !(getUri() != null ? !getUri().equals(that.getUri()) : that.getUri() != null);

    }

    @Override
    public int hashCode() {
        return getUri() != null ? getUri().hashCode() : 0;
    }
}

