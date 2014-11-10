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
package org.fabric3.datasource.spi;

import java.util.Map;
import javax.sql.DataSource;

/**
 * A registry of datasources.
 */
public interface DataSourceRegistry {

    /**
     * Gets a named datasource from the registry.
     *
     * @param name the name of the datasource.
     * @return Named datasource.
     */
    DataSource getDataSource(String name);

    /**
     * Returns all registered datasources.
     *
     * @return all registered datasources
     */
    Map<String, DataSource> getDataSources();

    /**
     * Registers a datasource by name.
     *
     * @param name       the ame of the datasource.
     * @param dataSource the datasource to be registered.
     */
    void register(String name, DataSource dataSource);

    /**
     * Unregisters a datasource.
     *
     * @param name the datasource name
     * @return the unregistered datasource or null if not found
     */
    DataSource unregister(String name);
}
