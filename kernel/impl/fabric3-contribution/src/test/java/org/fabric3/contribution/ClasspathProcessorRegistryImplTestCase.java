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
package org.fabric3.contribution;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.model.os.Library;

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
