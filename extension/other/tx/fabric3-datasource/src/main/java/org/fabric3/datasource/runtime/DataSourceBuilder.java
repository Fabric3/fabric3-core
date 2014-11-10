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
package org.fabric3.datasource.runtime;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.datasource.provision.PhysicalDataSourceResource;
import org.fabric3.datasource.spi.DataSourceFactory;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class DataSourceBuilder implements ResourceBuilder<PhysicalDataSourceResource> {
    private DataSourceFactory factory;

    public DataSourceBuilder(@Reference DataSourceFactory factory) {
        this.factory = factory;
    }

    public void build(PhysicalDataSourceResource definition) throws ContainerException {
        for (DataSourceConfiguration configuration : definition.getConfigurations()) {
            factory.create(configuration);
        }
    }

    public void remove(PhysicalDataSourceResource definition) throws ContainerException {
        for (DataSourceConfiguration configuration : definition.getConfigurations()) {
            factory.remove(configuration);
        }
    }
}