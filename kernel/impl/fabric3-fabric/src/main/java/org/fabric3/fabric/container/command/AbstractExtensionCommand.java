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

/**
 * Base class for extension commands.
 */
public abstract class AbstractExtensionCommand implements CompensatableCommand {
    private static final long serialVersionUID = 8299761365600303716L;
    private URI contribution;
    private URI provider;

    /**
     * Constructor.
     *
     * @param contribution the contribution URI providing the extension point
     * @param provider     the extension point provider URI
     */
    public AbstractExtensionCommand(URI contribution, URI provider) {
        this.contribution = contribution;
        this.provider = provider;
    }

    public URI getContribution() {
        return contribution;
    }

    public URI getProvider() {
        return provider;
    }

}