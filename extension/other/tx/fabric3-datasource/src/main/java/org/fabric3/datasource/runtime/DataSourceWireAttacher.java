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
package org.fabric3.datasource.runtime;

import javax.sql.DataSource;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.datasource.provision.DataSourceWireTargetDefinition;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches to a runtime DataSource.
 */
public class DataSourceWireAttacher implements TargetWireAttacher<DataSourceWireTargetDefinition> {
    private DataSourceRegistry registry;

    public DataSourceWireAttacher(@Reference DataSourceRegistry registry) {
        this.registry = registry;
    }

    public void attach(PhysicalWireSourceDefinition source, DataSourceWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        throw new AssertionError();
    }

    public void detach(PhysicalWireSourceDefinition source, DataSourceWireTargetDefinition target) throws Fabric3Exception {
        throw new AssertionError();
    }

    public Supplier<DataSource> createSupplier(DataSourceWireTargetDefinition target) throws Fabric3Exception {
        String dataSourceName = target.getDataSourceName();
        DataSource source = registry.getDataSource(dataSourceName);
        if (!target.isOptional() && source == null) {
            throw new Fabric3Exception("DataSource not found: " + dataSourceName);
        }
        return () -> source;
    }
}