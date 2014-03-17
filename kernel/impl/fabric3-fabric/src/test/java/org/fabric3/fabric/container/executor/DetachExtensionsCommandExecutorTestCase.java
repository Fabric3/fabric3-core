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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.container.executor;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.domain.command.DetachExtensionCommand;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.command.CommandExecutorRegistry;

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
