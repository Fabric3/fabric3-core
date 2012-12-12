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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.List;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.host.Environment;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.stream.InputStreamSource;
import org.fabric3.host.util.FileHelper;

/**
 * Loads the system configuration property for a runtime domain.
 */
public class SystemConfigLoaderTestCase extends TestCase {

    private static final String CONFIG = "<config>" +
            "<runtime domain='mydomain' mode='controller' jmxPort='1111'/>" +
            "   <web.server>" +
            "       <http port='8181'/>" +
            "   </web.server>" +
            "</config>";

    private static final String ZONE_CONFIG = "<config>" +
            "   <federation>" +
            "      <zoneName>zone1</zoneName>" +
            "   </federation>" +
            "</config>";

    private static final String PRODUCT_CONFIG = "<config><runtime product='Foo'/></config>";

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

    private static final String DEPLOY_DIRS = "<config>" +
            "   <deploy.directories>" +
            "       <deploy.directory>foo</deploy.directory>" +
            "       <deploy.directory>bar</deploy.directory>" +
            "   </deploy.directories>" +
            "</config>";

    private static final String ENVIRONMENT = "<config><runtime environment='test'/></config>";

    private static final String DEFAULT_ENVIRONMENT = "<config><runtime/></config>";

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

    public void testParseRuntimeMode() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        assertEquals(RuntimeMode.CONTROLLER, loader.parseRuntimeMode(systemConfig));
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

    public void testParseProductName() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(PRODUCT_CONFIG.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        String name = loader.parseProductName(systemConfig);
        assertEquals("Foo", name);
    }

    public void testParseDefaultProductName() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG_DEFAULT.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        String name = loader.parseProductName(systemConfig);
        assertEquals("Fabric3", name);
    }

    public void testParseZoneName() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(ZONE_CONFIG.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        assertEquals("zone1", loader.parseZoneName(systemConfig));
    }

    public void testParseDefaultZoneName() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(CONFIG_DEFAULT.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        assertEquals("default.zone", loader.parseZoneName(systemConfig));
    }

    public void testParseEnvironment() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(ENVIRONMENT.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        assertEquals("test", loader.parseEnvironment(systemConfig));
    }

    public void testDefaultEnvironment() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(DEFAULT_ENVIRONMENT.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        assertEquals(Environment.PRODUCTION, loader.parseEnvironment(systemConfig));
    }

    public void testParseDeployDirectories() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(DEPLOY_DIRS.getBytes());
        InputStreamSource source = new InputStreamSource("stream", stream);
        Document systemConfig = loader.loadSystemConfig(source);
        List<File> dirs = loader.parseDeployDirectories(systemConfig);
        assertEquals(2, dirs.size());
        assertTrue(dirs.get(0).getName().equals("foo") || dirs.get(0).getName().equals("bar"));
        assertTrue(dirs.get(1).getName().equals("foo") || dirs.get(1).getName().equals("bar"));
    }

    public void testCreateDefaultSystemConfig() throws Exception {
        SystemConfigLoader loader = new SystemConfigLoader();
        Document systemConfig = loader.createDefaultSystemConfig();
        Element root = systemConfig.getDocumentElement();
        assertEquals("values", root.getNodeName());
        assertEquals(1, root.getElementsByTagName("config").getLength());
    }

    public void testLoadSystemConfig() throws Exception {
        File dir = new File("testdir");
        try {
            if (dir.exists()) {
                FileHelper.forceDelete(dir);
            }
            assertTrue(dir.mkdir());
            File file = new File(dir, "systemConfig.xml");
            FileHelper.write(new ByteArrayInputStream(CONFIG.getBytes()), file);
            SystemConfigLoader loader = new SystemConfigLoader();
            Document systemConfig = loader.loadSystemConfig(dir);
            Element root = systemConfig.getDocumentElement();
            assertEquals("values", root.getNodeName());
        } finally {
            if (dir.exists()) {
                FileHelper.forceDelete(dir);
            }
        }
    }
}