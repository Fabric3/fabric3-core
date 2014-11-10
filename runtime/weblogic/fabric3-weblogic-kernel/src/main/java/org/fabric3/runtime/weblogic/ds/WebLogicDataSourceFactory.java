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
package org.fabric3.runtime.weblogic.ds;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.datasource.spi.DataSourceFactory;
import org.fabric3.datasource.spi.DataSourceFactoryException;

/**
 *
 */
@EagerInit
public class WebLogicDataSourceFactory implements DataSourceFactory {

    public void create(DataSourceConfiguration configuration) throws DataSourceFactoryException {
        throw new DataSourceFactoryException("DataSource creation not supported on WebLogic");
    }

    public void remove(DataSourceConfiguration configuration) throws DataSourceFactoryException {
        throw new DataSourceFactoryException("DataSource creation not supported on WebLogic");
    }
}
