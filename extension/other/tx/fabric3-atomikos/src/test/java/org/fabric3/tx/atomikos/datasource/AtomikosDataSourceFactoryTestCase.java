/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/

package org.fabric3.tx.atomikos.datasource;

import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.datasource.spi.DataSourceConfiguration;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.datasource.spi.DataSourceType;
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
