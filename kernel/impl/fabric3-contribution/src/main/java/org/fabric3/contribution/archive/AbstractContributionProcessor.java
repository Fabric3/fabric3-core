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
package org.fabric3.contribution.archive;

import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * The base ContributionProcessor implementation.
 */
@EagerInit
@Service(ContributionProcessor.class)
public abstract class AbstractContributionProcessor implements ContributionProcessor {
    protected ProcessorRegistry registry;

    /**
     * Sets the ContributionProcessorRegistry that this processor should register with/
     *
     * @param registry the ContributionProcessorRegistry that this processor should register with
     */
    @Reference
    public void setContributionProcessorRegistry(ProcessorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Initialize the processor and registers with the processor registry.
     */
    @Init
    public void start() {
        registry.register(this);
    }

    /**
     * Shuts the processor down and unregisters from the processor registry.
     */
    @Destroy
    public void stop() {
        registry.unregister(this);
    }

}
