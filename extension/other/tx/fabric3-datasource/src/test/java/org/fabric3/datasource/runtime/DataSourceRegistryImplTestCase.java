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

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class DataSourceRegistryImplTestCase extends TestCase {

    public void testRegisterUnresgister() throws Exception {
        DataSource dataSource = EasyMock.createNiceMock(DataSource.class);

        EasyMock.replay(dataSource);

        DataSourceRegistryImpl registry = new DataSourceRegistryImpl();

        registry.register("datasource", dataSource);
        assertNotNull(registry.getDataSource("datasource"));
        assertTrue(registry.getDataSourceNames().contains("datasource"));
        assertTrue(registry.getDataSources().containsKey("datasource"));

        assertNotNull(registry.unregister("datasource"));
        assertNull(registry.getDataSource("datasource"));

        EasyMock.verify(dataSource);
    }
}
