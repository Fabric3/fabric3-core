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

package org.fabric3.tx.atomikos.datasource;

import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;
import org.fabric3.spi.management.ManagementService;

/**
 *
 */
public class AtomikosDataSourceFactoryTestCase extends TestCase {

    public void testXADataSourceRegisterUnregister() throws Exception {
        DataSource datasource = EasyMock.createMock(DataSource.class);
        DataSourceRegistry registry = EasyMock.createMock(DataSourceRegistry.class);
        EasyMock.expect(registry.getDataSource(EasyMock.eq("datasource"))).andReturn(null);
        registry.register(EasyMock.eq("datasource"), EasyMock.isA(DataSource.class));
        EasyMock.expect(registry.unregister(EasyMock.eq("datasource"))).andReturn(datasource);

        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        managementService.export(EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(DataSourceWrapper.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.replay(registry, managementService, datasource);

        AtomikosDataSourceFactory factory = new AtomikosDataSourceFactory(registry, managementService);
        factory.init();

        DataSourceConfiguration configuration = new DataSourceConfiguration("datasource", "org.fabric3.datasource", DataSourceType.XA);
        factory.create(configuration);
        factory.remove(configuration);

        factory.destroy();
        EasyMock.verify(registry, managementService, datasource);
    }

    public void testNoXADataSourceRegisterUnregister() throws Exception {
        DataSource datasource = EasyMock.createMock(DataSource.class);
        DataSourceRegistry registry = EasyMock.createMock(DataSourceRegistry.class);
        EasyMock.expect(registry.getDataSource(EasyMock.eq("datasource"))).andReturn(null);
        registry.register(EasyMock.eq("datasource"), EasyMock.isA(DataSource.class));
        EasyMock.expect(registry.unregister(EasyMock.eq("datasource"))).andReturn(datasource);

        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        managementService.export(EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(DataSourceWrapper.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.replay(registry, managementService, datasource);

        AtomikosDataSourceFactory factory = new AtomikosDataSourceFactory(registry, managementService);
        factory.init();

        DataSourceConfiguration configuration = new DataSourceConfiguration("datasource", "org.fabric3.datasource", DataSourceType.NON_XA);
        factory.create(configuration);
        factory.remove(configuration);

        factory.destroy();
        EasyMock.verify(registry, managementService, datasource);
    }

    public void testDataSourceCloseDataSourceOnDestroy() throws Exception {
        DataSource datasource = EasyMock.createMock(DataSource.class);
        DataSourceRegistry registry = EasyMock.createMock(DataSourceRegistry.class);
        EasyMock.expect(registry.getDataSource(EasyMock.eq("datasource"))).andReturn(null);
        registry.register(EasyMock.eq("datasource"), EasyMock.isA(DataSource.class));
        EasyMock.expect(registry.unregister(EasyMock.eq("datasource"))).andReturn(datasource);

        ManagementService managementService = EasyMock.createMock(ManagementService.class);
        managementService.export(EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(String.class),
                                 EasyMock.isA(DataSourceWrapper.class));
        managementService.remove(EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.replay(registry, managementService, datasource);

        AtomikosDataSourceFactory factory = new AtomikosDataSourceFactory(registry, managementService);
        factory.init();

        DataSourceConfiguration configuration = new DataSourceConfiguration("datasource", "org.fabric3.datasource", DataSourceType.XA);
        factory.create(configuration);

        factory.destroy();
        EasyMock.verify(registry, managementService, datasource);
    }

}
