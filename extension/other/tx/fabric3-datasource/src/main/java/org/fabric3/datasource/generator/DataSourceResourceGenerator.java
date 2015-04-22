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
package org.fabric3.datasource.generator;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.resource.datasource.DataSourceResource;
import org.fabric3.datasource.provision.PhysicalDataSourceResource;
import org.fabric3.spi.domain.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalResource;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Generates DataSource definitions.
 */
@EagerInit
public class DataSourceResourceGenerator implements ResourceGenerator<DataSourceResource> {

    public PhysicalResource generateResource(LogicalResource<DataSourceResource> resource) throws Fabric3Exception {
        return new PhysicalDataSourceResource(resource.getDefinition().getConfigurations());
    }
}