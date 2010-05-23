/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.host.runtime.PortRange;
import org.fabric3.host.stream.InputStreamSource;

/**
 * Loads the system configuration property for a runtime domain.
 *
 * @version $Revision$ $Date$
 */
public class SystemConfigLoaderTestCase extends TestCase {

    private static final String CONFIG = "<config>" +
            "<runtime domain='mydomain' jmxPort='1111'/>" +
            "   <web.server>" +
            "       <http port='8181'/>" +
            "   </web.server>" +
            "</config>";

    private static final String CONFIG_JMX_RANGE = "<config>" +
            "<runtime domain='mydomain' jmxPort='1111-2222'/>" +
            "   <web.server>" +
            "       <http port='8181'/>" +
            "   </web.server>" +
            "</config>";

    private static final String CONFIG_DEFAULT = "<config>" +
            "   <web.server>" +
            "       <http port='8181'/>" +
            "   </web.server>" +
            "</config>";

    private static final String CONFIG_MONITOR =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<runtime.monitor>" +
                    "<configuration>" +
                    "   <appender name='CUSTOM' class='org.fabric3.monitor.runtime.TestAppender'>" +
                    "       <encoder><pattern>test-appender: %msg</pattern></encoder>" +
                    "   </appender>" +
                    "</configuration>" +
                    "</runtime.monitor>";

    public void testGetMonitorConfiguration() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG_MONITOR.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        Element element = loader.getMonitorConfiguration("runtime.monitor", systemConfig);
        assertEquals(1, element.getElementsByTagName("logger").getLength());
        assertEquals(1, element.getElementsByTagName("appender-ref").getLength());
    }

    public void testParseDomainName() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        URI uri = loader.parseDomainName(systemConfig);
        URI result = URI.create("fabric3://mydomain");
        assertEquals(result, uri);
    }

    public void testParseDefaultDomainName() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG_DEFAULT.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        URI uri = loader.parseDomainName(systemConfig);
        URI result = URI.create("fabric3://domain");
        assertEquals(result, uri);
    }

    public void testParseJmxPort() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        PortRange range = loader.parseJmxPort(systemConfig);
        assertEquals(1111, range.getMinimum());
        assertEquals(1111, range.getMaximum());
    }

    public void testParseJmxPortRange() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG_JMX_RANGE.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        PortRange range = loader.parseJmxPort(systemConfig);
        assertEquals(1111, range.getMinimum());
        assertEquals(2222, range.getMaximum());
    }

    public void testParseDefaultJmxPort() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG_DEFAULT.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        PortRange range = loader.parseJmxPort(systemConfig);
        assertEquals(1199, range.getMinimum());
        assertEquals(1199, range.getMaximum());
    }
}