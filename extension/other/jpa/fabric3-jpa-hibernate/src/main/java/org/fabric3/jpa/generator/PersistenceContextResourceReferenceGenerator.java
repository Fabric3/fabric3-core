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
package org.fabric3.jpa.generator;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.jpa.model.PersistenceContextResourceReference;
import org.fabric3.jpa.override.OverrideRegistry;
import org.fabric3.jpa.provision.PersistenceContextWireTargetDefinition;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalResourceReference;

/**
 *
 */
@EagerInit
public class PersistenceContextResourceReferenceGenerator implements ResourceReferenceGenerator<PersistenceContextResourceReference> {
    private OverrideRegistry registry;

    public PersistenceContextResourceReferenceGenerator(@Reference OverrideRegistry registry) {
        this.registry = registry;
    }

    public PersistenceContextWireTargetDefinition generateWireTarget(LogicalResourceReference<PersistenceContextResourceReference> logicalResourceReference) {
        PersistenceContextResourceReference resource = logicalResourceReference.getDefinition();
        String unitName = resource.getUnitName();
        PersistenceContextWireTargetDefinition definition = new PersistenceContextWireTargetDefinition(unitName);
        PersistenceOverrides overrides = registry.resolve(unitName);
        if (overrides != null) {
            definition.setOverrides(overrides);
        }
        boolean multiThreaded = resource.isMultiThreaded();
        definition.setOptimizable(true);
        definition.setMultiThreaded(multiThreaded);
        return definition;
    }

}