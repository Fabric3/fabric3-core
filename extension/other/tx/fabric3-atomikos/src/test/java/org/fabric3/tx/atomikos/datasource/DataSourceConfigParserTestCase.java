/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.datasource.spi.DataSourceConfiguration;
import org.fabric3.datasource.spi.DataSourceType;

/**
 * @version $Rev: 7148 $ $Date: 2009-06-14 17:18:27 -0700 (Sun, 14 Jun 2009) $
 */
public class DataSourceConfigParserTestCase extends TestCase {
    private static final String XML = "<foo><value>" +
            "<datasources>" +
            "   <datasource name='test' driver='foo.Bar' type='xa'>" +
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