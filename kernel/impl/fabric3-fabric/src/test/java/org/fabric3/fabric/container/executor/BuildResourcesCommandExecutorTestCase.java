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

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.command.BuildResourcesCommand;
import org.fabric3.fabric.container.command.BuildResourcesCommandExecutor;
import org.fabric3.spi.container.builder.ResourceBuilder;
import org.fabric3.fabric.container.command.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalResource;

/**
 *
 */
public class BuildResourcesCommandExecutorTestCase extends TestCase {

    public void testExecute() throws Exception {
        CommandExecutorRegistry registry = EasyMock.createMock(CommandExecutorRegistry.class);
        registry.register(EasyMock.eq(BuildResourcesCommand.class), EasyMock.isA(BuildResourcesCommandExecutor.class));
        ResourceBuilder<MockResource> builder = EasyMock.createMock(ResourceBuilder.class);
        builder.build(EasyMock.isA(MockResource.class));
        EasyMock.replay(registry, builder);

        Map<Class<?>, ResourceBuilder> builders = Collections.<Class<?>, ResourceBuilder>singletonMap(MockResource.class, builder);

        BuildResourcesCommandExecutor executor = new BuildResourcesCommandExecutor(registry);
        executor.setBuilders(builders);
        executor.init();

        PhysicalResource physicalResource = new MockResource();
        BuildResourcesCommand command = new BuildResourcesCommand(Collections.singletonList(physicalResource));
        executor.execute(command);

        EasyMock.verify(registry, builder);
    }

    private class MockResource extends PhysicalResource {
    }
}
