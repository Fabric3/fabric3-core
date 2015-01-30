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
package org.fabric3.fabric.container.executor;

import java.util.Map;

import org.fabric3.fabric.container.command.BuildResourcesCommand;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Builds resources defined in a composite on a runtime.
 */
@EagerInit
public class BuildResourcesCommandExecutor implements CommandExecutor<BuildResourcesCommand> {
    private CommandExecutorRegistry executorRegistry;
    private Map<Class<?>, ResourceBuilder> builders;

    public BuildResourcesCommandExecutor(@Reference CommandExecutorRegistry registry) {
        this.executorRegistry = registry;
    }

    @Reference(required = false)
    public void setBuilders(Map<Class<?>, ResourceBuilder> builders) {
        this.builders = builders;
    }

    @Init
    public void init() {
        executorRegistry.register(BuildResourcesCommand.class, this);
    }

    public void execute(BuildResourcesCommand command) throws ContainerException {
        for (PhysicalResourceDefinition definition : command.getDefinitions()) {
            build(definition);
        }
    }

    @SuppressWarnings("unchecked")
    private void build(PhysicalResourceDefinition definition) throws ContainerException {
        ResourceBuilder builder = builders.get(definition.getClass());
        if (builder == null) {
            throw new ContainerException("Builder not found for " + definition.getClass().getName());
        }
        builder.build(definition);
    }

}