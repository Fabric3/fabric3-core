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
package org.fabric3.fabric.container.executor;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.command.DisposeComponentCommand;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.container.builder.component.ComponentBuilderListener;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 *
 */
public class DisposeComponentCommandExecutorTestCase extends TestCase {


    @SuppressWarnings({"unchecked"})
    public void testUnregisterAndNotifyListener() throws Exception {
        PhysicalComponentDefinition definition = new MockDefinition();

        CommandExecutorRegistry registry = EasyMock.createMock(CommandExecutorRegistry.class);

        Component component = EasyMock.createMock(Component.class);

        ComponentBuilder builder = EasyMock.createMock(ComponentBuilder.class);
        builder.dispose(EasyMock.isA(PhysicalComponentDefinition.class), EasyMock.isA(Component.class));

        ComponentManager componentManager = EasyMock.createMock(ComponentManager.class);
        EasyMock.expect(componentManager.unregister(URI.create("test"))).andReturn(component);

        ComponentBuilderListener listener = EasyMock.createMock(ComponentBuilderListener.class);
        listener.onDispose(EasyMock.isA(Component.class), EasyMock.isA(PhysicalComponentDefinition.class));

        EasyMock.replay(componentManager, builder, component, listener, registry);

        DisposeComponentCommandExecutor executor = new DisposeComponentCommandExecutor(null, componentManager);
        Map<Class<?>, ComponentBuilder> map = Collections.<Class<?>, ComponentBuilder>singletonMap(MockDefinition.class, builder);
        executor.setBuilders(map);

        executor.setListeners(Collections.singletonList(listener));

        DisposeComponentCommand command = new DisposeComponentCommand(definition);
        executor.execute(command);

        EasyMock.verify(componentManager, builder, component, listener, registry);
    }

    private class MockDefinition extends PhysicalComponentDefinition {
        private static final long serialVersionUID = -809769047230911419L;

        private MockDefinition() {
            setComponentUri(URI.create("test"));
        }
    }
}
