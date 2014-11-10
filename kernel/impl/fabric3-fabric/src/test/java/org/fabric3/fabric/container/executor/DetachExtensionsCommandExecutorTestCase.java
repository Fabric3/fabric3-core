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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.command.DetachExtensionCommand;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;

/**
 *
 */
public class DetachExtensionsCommandExecutorTestCase extends TestCase {
    private Field field;


    public void testDetachExtension() throws Exception {
        URI contributionUri = URI.create("contribution");
        URI providerUri = URI.create("provider");

        ClassLoader parent = getClass().getClassLoader();
        MultiParentClassLoader contributionLoader = new MultiParentClassLoader(contributionUri, parent);
        MultiParentClassLoader providerLoader = new MultiParentClassLoader(providerUri, parent);
        contributionLoader.addExtensionClassLoader(providerLoader);

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);

        ClassLoaderRegistry classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(contributionUri)).andReturn(contributionLoader);
        EasyMock.expect(classLoaderRegistry.getClassLoader(providerUri)).andReturn(providerLoader);

        EasyMock.replay(info, executorRegistry, classLoaderRegistry);

        DetachExtensionCommandExecutor executor = new DetachExtensionCommandExecutor(info, executorRegistry, classLoaderRegistry);

        DetachExtensionCommand command = new DetachExtensionCommand(contributionUri, providerUri);
        executor.execute(command);
        EasyMock.verify(info, executorRegistry, classLoaderRegistry);


        assertFalse(((Collection) field.get(contributionLoader)).contains(providerLoader));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        field = MultiParentClassLoader.class.getDeclaredField("extensions");
        field.setAccessible(true);
    }
}
