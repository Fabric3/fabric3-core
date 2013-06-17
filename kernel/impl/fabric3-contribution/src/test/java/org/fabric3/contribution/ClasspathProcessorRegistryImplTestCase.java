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
package org.fabric3.contribution;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;

/**
 *
 */
public class ClasspathProcessorRegistryImplTestCase extends TestCase {
    private ClasspathProcessorRegistryImpl registry;
    private ClasspathProcessor processor;
    private URL url;
    private URL processedUrl;

    public void testRegisterUnregister() throws Exception {
        registry.register(processor);
        registry.unregister(processor);
    }

    public void testProcess() throws Exception {
        EasyMock.expect(processor.canProcess(url)).andReturn(true);
        List<URL> classpath = Collections.singletonList(processedUrl);
        List<Library> libraries = Collections.emptyList();
        EasyMock.expect(processor.process(url, libraries)).andReturn(classpath);

        EasyMock.replay(processor);
        assertTrue(registry.process(url, Collections.<Library>emptyList()).contains(processedUrl));
        EasyMock.verify(processor);
    }

    public void testNoProcess() throws Exception {
        EasyMock.expect(processor.canProcess(url)).andReturn(false);

        EasyMock.replay(processor);
        List<Library> libraries = Collections.emptyList();
        assertTrue(registry.process(url, libraries).contains(url));
        EasyMock.verify(processor);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        url = new URL("file://url");
        processedUrl = new URL("file://processed");
        registry = new ClasspathProcessorRegistryImpl();
        processor = EasyMock.createMock(ClasspathProcessor.class);
        registry.register(processor);
    }


}
