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
import org.fabric3.api.host.ContainerException;
import org.fabric3.datasource.provision.DataSourceWireTargetDefinition;
import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public class DataSourceWireAttacherTestCase extends TestCase {

    public void testCreateObjectFactory() throws Exception {
        DataSource dataSource = EasyMock.createNiceMock(DataSource.class);

        DataSourceRegistry registry = EasyMock.createNiceMock(DataSourceRegistry.class);
        registry.getDataSource("datasource");
        EasyMock.expectLastCall().andReturn(dataSource);
        EasyMock.replay(dataSource, registry);

        DataSourceWireAttacher attacher = new DataSourceWireAttacher(registry);

        DataSourceWireTargetDefinition definition = new DataSourceWireTargetDefinition("datasource", false);
        ObjectFactory<DataSource> factory = attacher.createObjectFactory(definition);
        assertEquals(dataSource, factory.getInstance());

        EasyMock.verify(dataSource, registry);
    }


    public void testOptionalCreateObjectFactory() throws Exception {

        DataSourceRegistry registry = EasyMock.createNiceMock(DataSourceRegistry.class);
        registry.getDataSource("datasource");
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(registry);

        DataSourceWireAttacher attacher = new DataSourceWireAttacher(registry);

        DataSourceWireTargetDefinition definition = new DataSourceWireTargetDefinition("datasource", true);
        ObjectFactory<DataSource> factory = attacher.createObjectFactory(definition);
        assertNull(factory.getInstance());    // datasource not found, inject null

        EasyMock.verify(registry);
    }

    public void testRequiredCreateObjectFactory() throws Exception {

        DataSourceRegistry registry = EasyMock.createNiceMock(DataSourceRegistry.class);
        registry.getDataSource("datasource");
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(registry);

        DataSourceWireAttacher attacher = new DataSourceWireAttacher(registry);

        DataSourceWireTargetDefinition definition = new DataSourceWireTargetDefinition("datasource", false);
        try {
            attacher.createObjectFactory(definition);
            fail();
        } catch (ContainerException e) {
            // expected
        }
        EasyMock.verify(registry);
    }

}
