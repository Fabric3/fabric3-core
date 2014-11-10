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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.datasource.spi.DataSourceRegistry;

/**
 * Default DataSourceRegistry implementation.
 */
@Management(name = "DataSourceRegistry", path = "/runtime/datasources")
public class DataSourceRegistryImpl implements DataSourceRegistry {
    private Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @ManagementOperation(path = "/")
    public Set<String> getDataSourceNames() {
        return dataSources.keySet();
    }

    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public void register(String name, DataSource dataSource) {
        dataSources.put(name, dataSource);
    }

    public DataSource unregister(String name) {
        return dataSources.remove(name);
    }
}
