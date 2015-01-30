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
package org.fabric3.datasource.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceResource;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;

/**
 *
 */
public class DataSourceResourceLoaderTestCase extends TestCase {
    private static final String SINGLE_DATASOURCE =
            "<datasources>" +
                    "   <datasource name='test' aliases='foo,bar' driver='foo.Bar' type='xa' url='jdbc:test'>" +
                    "      <username>user</username>" +
                    "      <password>pass</password>" +
                    "      <maxPoolSize>10</maxPoolSize>" +
                    "      <minPoolSize>1</minPoolSize>" +
                    "      <connectionTimeout>20000</connectionTimeout>" +
                    "      <loginTimeout>30000</loginTimeout>" +
                    "      <maintenanceInterval>40000</maintenanceInterval>" +
                    "      <maxIdle>50000</maxIdle>" +
                    "      <poolSize>30</poolSize>" +
                    "      <reap>40</reap>" +
                    "      <query>test query</query>" +
                    "      <foo>bar</foo>" +
                    "   </datasource>" +
                    "</datasources>";

    private static final String MULTIPLE_DATASOURCE =
            "<datasources>" +
                    "   <datasource name='test' driver='foo.Bar' type='xa' url='jdbc:test'>" +
                    "      <username>user</username>" +
                    "      <password>pass</password>" +
                    "   </datasource>" +
                    "   <datasource name='test2' driver='foo.Bar2' type='non_xa' url='jdbc:test2'>" +
                    "      <username>user2</username>" +
                    "      <password>pass2</password>" +
                    "   </datasource>" +
                    "</datasources>";

    private DataSourceResourceLoader loader = new DataSourceResourceLoader();

    public void testSingleDataSourceParse() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(SINGLE_DATASOURCE.getBytes()));

        DataSourceResource resource = loader.load(reader, context);

        assertFalse(context.hasErrors());
        DataSourceConfiguration configuration = resource.getConfigurations().get(0);
        assertTrue(configuration.getAliases().contains("foo"));
        assertTrue(configuration.getAliases().contains("bar"));
        assertEquals(10, configuration.getMaxPoolSize());
        assertEquals(1, configuration.getMinPoolSize());
        assertEquals(20000, configuration.getConnectionTimeout());
        assertEquals(30000, configuration.getLoginTimeout());
        assertEquals(40000, configuration.getMaintenanceInterval());
        assertEquals(50000, configuration.getMaxIdle());
        assertEquals(30, configuration.getPoolSize());
        assertEquals(40, configuration.getReap());
        assertEquals("test query", configuration.getQuery());
        assertEquals("bar", configuration.getProperty("foo"));

        validateDataSource1(configuration);

    }

    public void testMultipleDataSourceParse() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(MULTIPLE_DATASOURCE.getBytes()));
        DataSourceResource resource = loader.load(reader, context);
        assertFalse(context.hasErrors());
        assertEquals(2, resource.getConfigurations().size());
        for (DataSourceConfiguration configuration : resource.getConfigurations()) {
            if ("test".equals(configuration.getName())) {
                validateDataSource1(configuration);
            } else if ("test2".equals(configuration.getName())) {
                validateDataSource2(configuration);
            } else {
                fail("Invalid datasource configuration");
            }
        }
        DataSourceConfiguration configuration = resource.getConfigurations().get(0);
        assertEquals("test", configuration.getName());

    }

    private void validateDataSource1(DataSourceConfiguration configuration) {
        assertEquals("test", configuration.getName());
        assertEquals("foo.Bar", configuration.getDriverClass());
        assertEquals(DataSourceType.XA, configuration.getType());
        assertEquals("user", configuration.getProperty("username"));
        assertEquals("pass", configuration.getProperty("password"));
        assertEquals("jdbc:test", configuration.getUrl());
    }

    private void validateDataSource2(DataSourceConfiguration configuration) {
        assertEquals("test2", configuration.getName());
        assertEquals("foo.Bar2", configuration.getDriverClass());
        assertEquals(DataSourceType.NON_XA, configuration.getType());
        assertEquals("user2", configuration.getProperty("username"));
        assertEquals("pass2", configuration.getProperty("password"));
        assertEquals("jdbc:test2", configuration.getUrl());
    }

}