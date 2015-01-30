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
package org.fabric3.contribution.archive;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;

/**
 *
 */
public class JarClasspathProcessorTestCase extends TestCase {
    private JarClasspathProcessor processor;

    /**
     * Verifies processing when no jars are present in META-INF/lib
     *
     * @throws Exception
     */
    public void testExpansionNoLibraries() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/1/test.jar");
        List<Library> libraries = Collections.emptyList();
        List<URL> urls = processor.process(location, libraries);
        assertEquals(1, urls.size());
        assertEquals(location, urls.get(0));
    }

    /**
     * Verifies jars in META-INF/lib are added to the classpath
     *
     * @throws Exception
     */
    public void testExpansionWithLibraries() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/2/testWithLibraries.jar");
        List<Library> libraries = Collections.emptyList();
        List<URL> urls = processor.process(location, libraries);
        assertEquals(2, urls.size());
        assertEquals(location, urls.get(0));
    }

    public void testExplodeJars() throws Exception {
        processor.setExplodeJars(true);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/2/testWithLibraries.jar");
        List<Library> libraries = Collections.emptyList();
        List<URL> classpath = processor.process(location, libraries);
        assertEquals(2, classpath.size());
        assertEquals(location, classpath.get(0));
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClasspathProcessorRegistry registry = EasyMock.createNiceMock(ClasspathProcessorRegistry.class);
        HostInfo info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(info.getTempDir()).andReturn(new File(System.getProperty("java.io.tmpdir"), ".f3")).atLeastOnce();
        EasyMock.replay(info);
        processor = new JarClasspathProcessor(registry, info);
    }
}
