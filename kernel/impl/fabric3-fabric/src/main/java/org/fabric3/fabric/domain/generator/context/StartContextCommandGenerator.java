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
package org.fabric3.fabric.domain.generator.context;

import java.util.List;

import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Generates commands to start component contexts in a zone.
 */
public interface StartContextCommandGenerator {

    /**
     * Generate the commands.
     *
     * @param components  the set of component being deployed
     * @return the commands ordered according to their deployable composite and the order it is deployed.
     * @throws GenerationException if a generation exception occurs
     */
    List<CompensatableCommand> generate(List<LogicalComponent<?>> components) throws GenerationException;

}
