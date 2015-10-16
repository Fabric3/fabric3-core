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
package org.fabric3.resource.generator;

import org.fabric3.api.model.type.resource.application.ApplicationResource;
import org.fabric3.resource.provision.PhysicalApplicationResource;
import org.fabric3.spi.domain.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalResource;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class ApplicationResourceGenerator implements ResourceGenerator<ApplicationResource> {

    public PhysicalResource generateResource(LogicalResource<ApplicationResource> resource) {
        String name = resource.getDefinition().getName();
        return new PhysicalApplicationResource(name, resource.getDefinition().getSupplier());
    }
}