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
package org.fabric3.fabric.domain;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.Generator;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implements a domain for system components in a runtime. Fabric3 runtimes are constituted using SCA components and the runtime domain manages deployment of
 * those system components. When a runtime is booted, the runtime domain is provided with a set of primordial services for deploying system components. After
 * bootstrap, the runtime domain is reinjected with a new set of fully-configured deployment services.
 */
public class RuntimeDomain extends AbstractDomain {

    public RuntimeDomain(@Reference MetaDataStore metadataStore,
                         @Reference Generator generator,
                         @Reference LogicalModelInstantiator logicalModelInstantiator,
                         @Reference LogicalComponentManager logicalComponentManager,
                         @Reference Deployer deployer,
                         @Reference Collector collector,
                         @Reference ContributionHelper contributionHelper,
                         @Reference HostInfo info) {
        super(metadataStore, logicalComponentManager, generator, logicalModelInstantiator, deployer, collector, contributionHelper, info);
    }

    /**
     * Used for reinjection.
     *
     * @param generator the generator to inject
     */
    @Reference
    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    /**
     * Used for reinjection.
     *
     * @param deployer the Deployer to reinject
     */
    @Reference
    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

}
