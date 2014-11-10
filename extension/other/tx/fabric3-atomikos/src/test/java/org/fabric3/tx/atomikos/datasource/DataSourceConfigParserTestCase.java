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
package org.fabric3.tx.atomikos.datasource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;

/**
 *
 */
public class DataSourceConfigParserTestCase extends TestCase {
    private static final String XML = "<foo><value>" +
            "<datasources>" +
            "   <datasource name='test' aliases='foo,bar' driver='foo.Bar' type='xa'>" +
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
            "</datasources>" +
            "</value></foo>";

    private DataSourceConfigParser parser;
    private XMLStreamReader reader;


    public void testParse() throws Exception {
        List<DataSourceConfiguration> configurations = parser.parse(reader);
        assertEquals(1, configurations.size());
        DataSourceConfiguration configuration = configurations.get(0);
        assertEquals("test", configuration.getName());
        assertTrue(configuration.getAliases().contains("foo"));
        assertTrue(configuration.getAliases().contains("bar"));
        assertEquals("foo.Bar", configuration.getDriverClass());
        assertEquals(DataSourceType.XA, configuration.getType());
        assertEquals("user", configuration.getProperty("username"));
        assertEquals("pass", configuration.getProperty("password"));
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
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new DataSourceConfigParser();
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();


    }


}