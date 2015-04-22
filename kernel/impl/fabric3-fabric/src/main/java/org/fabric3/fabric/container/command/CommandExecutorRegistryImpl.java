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

import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class CommandExecutorRegistryImpl implements CommandExecutorRegistry {
    private Map<Class<? extends Command>, CommandExecutor<?>> executors = new HashMap<>();

    public <T extends Command> void register(Class<T> type, CommandExecutor<T> executor) {
        executors.put(type, executor);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Command> void execute(T command) {
        Class<? extends Command> clazz = command.getClass();
        CommandExecutor<T> executor = (CommandExecutor<T>) executors.get(clazz);
        if (executor == null) {
            throw new AssertionError("No registered executor for command: " + clazz.getName());
        }
        executor.execute(command);
    }
}
