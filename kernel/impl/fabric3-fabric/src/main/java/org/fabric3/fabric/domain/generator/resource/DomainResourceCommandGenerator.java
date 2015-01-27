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
package org.fabric3.fabric.domain.generator.resource;

import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * Creates commands to build and un-build domain-level resources. Domain-level resources are special-cased as they are not part of a deployed composite. That
 * is, they are contained by the domain composite, which is virtual.
 */
public interface DomainResourceCommandGenerator {

    /**
     * Generates a build command.
     *
     * @param resource the resource to build
     * @return the command
     * @throws GenerationException if a generation error is encountered
     */
    CompensatableCommand generateBuild(LogicalResource resource) throws GenerationException;

    /**
     * Generates an un-build command.
     *
     * @param resource the resource to build
     * @return the command
     * @throws GenerationException if a generation error is encountered
     */
    CompensatableCommand generateDispose(LogicalResource resource) throws GenerationException;

}