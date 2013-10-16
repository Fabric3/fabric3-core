/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.contribution.archive;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;

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
