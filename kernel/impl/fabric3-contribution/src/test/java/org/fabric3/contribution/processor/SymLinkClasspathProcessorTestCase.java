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
package org.fabric3.contribution.processor;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.os.Library;

/**
 *
 */
public class SymLinkClasspathProcessorTestCase extends TestCase {
    private SymLinkClasspathProcessor processor;
    private ClasspathProcessorRegistry registry;
    private URL file;

    public void testInitDestroy() throws Exception {
        registry.register(processor);
        registry.unregister(processor);
        EasyMock.replay(registry);

        processor.init();
        processor.destroy();
        EasyMock.verify(registry);
    }

    public void testCanProcess() throws Exception {
        assertTrue(processor.canProcess(file));
    }

    public void testProcess() throws Exception {
        List<Library> libraries = Collections.emptyList();
        assertFalse(processor.process(file, libraries).isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(ClasspathProcessorRegistry.class);
        processor = new SymLinkClasspathProcessor(registry);
        file = getClass().getResource("sym.contribution");
    }
}
