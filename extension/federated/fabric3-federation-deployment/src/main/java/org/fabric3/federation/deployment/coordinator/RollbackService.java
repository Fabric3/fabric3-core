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

import java.util.List;

import org.fabric3.spi.container.command.CompensatableCommand;

/**
 * Reverts the runtime to a previous state.
 */
public interface RollbackService {

    /**
     * Reverts the runtime to its previous deployment state.
     *
     * @throws RollbackException if an error occurs during rollback
     */
    void rollback() throws RollbackException;

    /**
     * Reverts the runtime state after a failure by executing a collection of compensating commands.
     *
     * @param commands the commands that failed
     * @param marker   the command index where the failure occured
     * @throws RollbackException if an error occurs during rollback
     */
    void rollback(List<CompensatableCommand> commands, int marker) throws RollbackException;

}