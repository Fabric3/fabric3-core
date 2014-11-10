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
package org.fabric3.spi.container.executor;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.command.Command;

/**
 * A registry of {@link CommandExecutor}s.
 */
public interface CommandExecutorRegistry {

    /**
     * Register the command executor
     *
     * @param type     the type of command the executor handles
     * @param executor the executor
     */
    <T extends Command> void register(Class<T> type, CommandExecutor<T> executor);

    /**
     * Dispatches a command to an executor.
     *
     * @param command the command to dispatch
     * @throws ContainerException if there is an error executing the command
     */
    <T extends Command> void execute(T command) throws ContainerException;
}
