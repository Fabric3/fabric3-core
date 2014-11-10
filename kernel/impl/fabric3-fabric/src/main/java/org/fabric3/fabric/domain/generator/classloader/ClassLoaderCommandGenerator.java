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
package org.fabric3.fabric.domain.generator.classloader;

import java.util.List;
import java.util.Map;

import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.domain.generator.GenerationException;

/**
 * Generates commands for provisioning and releasing classloaders on runtimes for a set of contributions being deployed or undeployed.
 */
public interface ClassLoaderCommandGenerator {

    /**
     * Generates classloader provisioning commands for a set of contributions being deployed.
     *
     * @param contributions the required contributions for the deployment, grouped by zone
     * @return the classloader provisioning commands grouped by zone where they are to be provisioned
     * @throws GenerationException if an error occurs during generation
     */
    Map<String, List<CompensatableCommand>> generate(Map<String, List<Contribution>> contributions) throws GenerationException;

    /**
     * Generates classloader release commands for a set of contributions being undeployed.
     *
     * @param contributions the required contributions for the deployment, grouped by zone
     * @return the classloader provisioning commands grouped by zone where they are being undeployed
     * @throws GenerationException if an error occurs during generation
     */
    Map<String, List<CompensatableCommand>> release(Map<String, List<Contribution>> contributions) throws GenerationException;

}
