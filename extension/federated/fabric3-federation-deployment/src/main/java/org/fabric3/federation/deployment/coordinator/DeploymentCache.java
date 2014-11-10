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
 */
package org.fabric3.federation.deployment.coordinator;

import org.fabric3.federation.deployment.command.DeploymentCommand;

/**
 * Caches the deployment command history. The history can be used to synchronize a runtime to a zone's current deployment state.
 */
public interface DeploymentCache {

    /**
     * Cache the deployment command.
     *
     * @param command the deployment command
     */
    void cache(DeploymentCommand command);

    /**
     * Reverts to the previous cached command.
     *
     * @return the command that was discarded or null if no command was cached
     */
    public DeploymentCommand undo();

    /**
     * Returns the current deployment command or null if one is not cached.
     *
     * @return the current deployment command or null.
     */
    DeploymentCommand get();

}
