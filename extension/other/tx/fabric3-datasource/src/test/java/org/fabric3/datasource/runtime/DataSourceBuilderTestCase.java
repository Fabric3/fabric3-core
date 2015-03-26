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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;
import org.fabric3.datasource.provision.PhysicalDataSourceResource;
import org.fabric3.datasource.spi.DataSourceFactory;

/**
 *
 */
public class DataSourceBuilderTestCase extends TestCase {

    public void testBuildRemove() throws Exception {
        DataSource dataSource = EasyMock.createNiceMock(DataSource.class);

        DataSourceFactory factory = EasyMock.createNiceMock(DataSourceFactory.class);
        factory.create(EasyMock.isA(DataSourceConfiguration.class));
        factory.remove(EasyMock.isA(DataSourceConfiguration.class));
        EasyMock.replay(dataSource, factory);

        DataSourceBuilder builder = new DataSourceBuilder(factory);

        DataSourceConfiguration configuration = new DataSourceConfiguration("datasource", "driver", DataSourceType.XA);
        List<DataSourceConfiguration> list = Collections.singletonList(configuration);
        PhysicalDataSourceResource resource = new PhysicalDataSourceResource(list);

        builder.build(resource);
        builder.remove(resource);


        EasyMock.verify(dataSource, factory);
    }
}
