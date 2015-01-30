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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.host.Environment;
import org.fabric3.api.host.stream.InputStreamSource;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.model.type.RuntimeMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    private static final String PRODUCT_CONFIG = "<config><runtime product='Foo'/></config>";

    private static final String CONFIG_DEFAULT = "<config>" +
                                                 "   <web.server>" +
                                                 "       <http port='8181'/>" +
                                                 "   </web.server>" +
                                                 "</config>";

    private static final String DEPLOY_DIRS = "<config>" +
                                              "   <deploy.directories>" +
                                              "       <deploy.directory>foo</deploy.directory>" +
                                              "       <deploy.directory>bar</deploy.directory>" +
                                              "   </deploy.directories>" +
                                              "</config>";

    private static final String ENVIRONMENT = "<config><runtime environment='test'/></config>";

    private static final String DEFAULT_ENVIRONMENT = "<config><runtime/></config>";

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
        assertEquals(RuntimeMode.VM, loader.parseRuntimeMode(systemConfig));
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