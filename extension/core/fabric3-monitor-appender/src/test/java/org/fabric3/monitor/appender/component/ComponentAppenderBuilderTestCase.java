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
 */
package org.fabric3.monitor.appender.component;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Names;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.ComponentManager;

/**
 *
 */
public class ComponentAppenderBuilderTestCase extends TestCase {
    private ComponentAppenderBuilder builder;
    private ComponentManager componentManager;

    public void testBuild() throws Exception {
        URI uri = URI.create(Names.RUNTIME_NAME + "/test");
        AtomicComponent component = EasyMock.createMock(AtomicComponent.class);

        EasyMock.expect(componentManager.getComponent(uri)).andReturn(component);
        EasyMock.replay(componentManager, component);

        Appender appender = builder.build(new PhysicalComponentAppenderDefinition("test"));
        assertNotNull(appender);

        EasyMock.verify(componentManager, component);
    }

    public void setUp() throws Exception {
        super.setUp();
        componentManager = EasyMock.createMock(ComponentManager.class);
        builder = new ComponentAppenderBuilder(componentManager);
    }
}
